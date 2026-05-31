package com.example.auctionmanagementsystem.server.handler;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.BidTransactionDAO;
import com.example.auctionmanagementsystem.dao.ItemDAO;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.model.ItemFactory;
import com.example.auctionmanagementsystem.model.Seller;
import com.example.auctionmanagementsystem.server.JsonUtil;
import com.example.auctionmanagementsystem.service.AdvancedAuctionService;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.observer.AuctionNotifier;
import com.example.auctionmanagementsystem.service.AuctionService;
import com.example.auctionmanagementsystem.service.BiddingService;
import com.example.auctionmanagementsystem.service.ImageStorageService;
import com.example.auctionmanagementsystem.service.ItemService;
import com.example.auctionmanagementsystem.service.PaymentService;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionHandler extends BaseHandler {
    //luu tam thoi
    private static final ConcurrentHashMap<Integer, Auction> AUCTION_CACHE = new ConcurrentHashMap<>();
    public static void evictFromCache(int auctionId) {
        AUCTION_CACHE.remove(auctionId);
    }
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        try {
            if (path.endsWith("/auction/list")) {
                handleList(ex);
            } else if (path.endsWith("/auction/detail")) {
                handleDetail(ex);
            } else if (path.endsWith("/auction/bid") && "POST".equals(method)) {
                handleBid(ex);
            } else if (path.endsWith("/auction/create") && "POST".equals(method)) {
                handleCreate(ex);
            } else if (path.endsWith("/auction/approve") && "POST".equals(method)) {
                handleApprove(ex);
            } else if (path.endsWith("/auction/reject") && "POST".equals(method)) {
                handleReject(ex);
            } else if (path.endsWith("/auction/end") && "POST".equals(method)) {
                handleEnd(ex);
            } else if (path.endsWith("/auction/autobid") && "POST".equals(method)) {
                handleAutoBid(ex);
            } else {
                sendJson(ex, 404, err("Not found"));
            }
        } catch (Exception e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }


    private void handleList(HttpExchange ex) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT a.id, a.item_id, a.seller_id, a.current_price, a.status, a.end_time,"
                + " i.name, i.item_type, i.image_path,"
                + " (SELECT COUNT(*) FROM bid_transaction b WHERE b.auction_id = a.id) AS bid_count,"
                + " e.brand"
                + " FROM auction a"
                + " LEFT JOIN items i ON a.item_id = i.id"
                + " LEFT JOIN electronics_items e ON i.id = e.item_id AND i.item_type = 'ELECTRONICS'"
                + " ORDER BY a.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("itemId", rs.getInt("item_id"));
                m.put("name", rs.getString("name"));
                String itemType = rs.getString("item_type");
                m.put("itemType", itemType);
                m.put("category", resolveCategory(itemType, rs.getString("brand")));
                m.put("imagePath", rs.getString("image_path"));
                m.put("price", rs.getDouble("current_price"));
                m.put("bids", rs.getInt("bid_count"));
                m.put("sellerId", rs.getInt("seller_id"));
                m.put("status", rs.getString("status"));
                Timestamp endTs = rs.getTimestamp("end_time");
                m.put("endTime", endTs != null ? endTs.toLocalDateTime().toString() : null);
                result.add(m);
            }
        }
        sendJson(ex, 200, result);
    }

    private String resolveCategory(String itemType, String brand) {
        if ("VEHICLE".equals(itemType)) return "Vehicle";
        if ("ART".equals(itemType)) return "Art";
        return "Electronics";
    }

    private void handleDetail(HttpExchange ex) throws Exception {
        int id = Integer.parseInt(queryParams(ex).getOrDefault("id", "0"));
        try (Connection conn = DatabaseConnection.getConnection()) {
            Auction auction = new AuctionService().getById(conn, id);
            if (auction == null) {
                sendJson(ex, 404, err("Auction not found"));
                return;
            }
            Item item = new ItemDAO().selectById(auction.getItem().getId(), conn);
            List<BidTransaction> bids = new BidTransactionDAO().selectByAuctionId(conn, id);

            Map<String, Object> m = new HashMap<>();
            m.put("id", auction.getId());
            m.put("currentPrice", auction.getCurrentPrice());
            m.put("startTime", auction.getStartTime() != null ? auction.getStartTime().toString() : null);
            m.put("endTime", auction.getEndTime() != null ? auction.getEndTime().toString() : null);
            m.put("status", auction.getStatus().name());
            m.put("sellerId", auction.getSeller() != null ? auction.getSeller().getId() : 0);
            m.put("highestBidderId",
                    auction.getHighestBidder() != null ? auction.getHighestBidder().getId() : null);
            m.put("rejectReason", auction.getRejectReason());
            m.put("itemId", item != null ? item.getId() : 0);
            m.put("itemName", item != null ? item.getName() : "");
            m.put("itemDescription", item != null ? item.getDescription() : "");
            m.put("itemImagePath", item != null ? item.getImagePath() : null);
            m.put("itemType", item != null ? item.getClass().getSimpleName().toUpperCase() : "ELECTRONICS");

            List<Map<String, Object>> bidList = new ArrayList<>();
            for (BidTransaction bt : bids) {
                Map<String, Object> b = new HashMap<>();
                b.put("id", bt.getId());
                b.put("amount", bt.getAmount());
                b.put("bidTime", bt.getTime() != null ? bt.getTime().toString() : null);
                bidList.add(b);
            }
            m.put("bids", bidList);

            AUCTION_CACHE.putIfAbsent(id, auction);
            sendJson(ex, 200, m);
        }
    }

    private void handleBid(HttpExchange ex) throws IOException {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int auctionId = body.get("auctionId").getAsInt();
        int bidderId = body.get("bidderId").getAsInt();
        double amount = body.get("amount").getAsDouble();
        Auction auction = getCachedAuction(auctionId);
        if (auction == null) {
            sendJson(ex, 200, err("Auction not found"));
            return;
        }

        Bidder bidder = new Bidder();
        bidder.setId(bidderId);
        java.time.LocalDateTime endTimeBefore = auction.getEndTime();
        try {
            new BiddingService().placeBid(auction, bidder, amount);

            // Thong bao gia moi toi tat ca client dang mo auction nay
            AuctionNotifier.getInstance().notifyNewBid(
                    auctionId, auction.getCurrentPrice(), "Bidder #" + bidderId);

            // Neu anti-sniping vua gia han, thong bao endTime moi toi client
            if (!auction.getEndTime().equals(endTimeBefore)) {
                AuctionNotifier.getInstance().notifyTimeExtended(
                        auctionId, auction.getEndTime().toString());
            }

            Map<String, Object> resp = ok();
            resp.put("newPrice", auction.getCurrentPrice());
            sendJson(ex, 200, resp);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            sendJson(ex, 200, err(cause != null ? cause.getMessage() : e.getMessage()));
        }
    }


    private void handleCreate(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int sellerId = body.get("sellerId").getAsInt();
        String itemType = body.get("itemType").getAsString();
        String name = body.get("name").getAsString();
        String desc = body.has("desc") && !body.get("desc").isJsonNull() ? body.get("desc").getAsString() : "";
        double price = body.get("price").getAsDouble();
        String imageFileName = body.has("imageFileName") && !body.get("imageFileName").isJsonNull() ? body.get("imageFileName").getAsString() : null;
        String endTimeStr = body.get("endTime").getAsString();

        Map<String, String> attrs = new HashMap<>();
        if (body.has("attributes")) {
            JsonObject attrsObj = body.getAsJsonObject("attributes");
            for (String key : attrsObj.keySet()) {
                attrs.put(key, attrsObj.get(key).getAsString());
            }
        }

        Item item = ItemFactory.createItem(itemType, name, desc, price, attrs);
        item.setImagePath(imageFileName);
        int itemId = new ItemService().createItem(item);
        item.setId(itemId);
        Seller seller = new Seller();
        seller.setId(sellerId);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        Auction auction = new Auction(item, seller, price, AuctionStatus.PENDING,
                LocalDateTime.now(), endTime);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                new AuctionService().createAuction(conn, auction);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                throw e;
            }
        }

        Map<String, Object> resp = ok();
        resp.put("auctionId", auction.getId());
        resp.put("itemId", item.getId());
        sendJson(ex, 200, resp);
    }

    private void handleApprove(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int auctionId = body.get("auctionId").getAsInt();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE auction SET status='OPEN' WHERE id=? AND status='PENDING'")) {
            ps.setInt(1, auctionId);
            ps.executeUpdate();
        }
        Auction cached = AUCTION_CACHE.get(auctionId);
        if (cached != null) cached.setStatus(AuctionStatus.OPEN);
        sendJson(ex, 200, ok());
    }

    private void handleReject(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int auctionId = body.get("auctionId").getAsInt();
        String reason = body.get("reason").getAsString();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE auction SET status='REJECTED', reject_reason=? WHERE id=? AND status='PENDING'")) {
            ps.setString(1, reason);
            ps.setInt(2, auctionId);
            ps.executeUpdate();
        }
        Auction cached = AUCTION_CACHE.get(auctionId);
        if (cached != null) {
            cached.setStatus(AuctionStatus.REJECTED);
            cached.setRejectReason(reason);
        }
        sendJson(ex, 200, ok()); 
    }

    private void handleEnd(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int auctionId = body.get("auctionId").getAsInt();
        Auction auction = getCachedAuction(auctionId);
        if (auction == null) {
            sendJson(ex, 200, err("Auction not found"));
            return;
        }

        double bidderBalance = -1;
        double sellerBalance = -1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            new AuctionService().endAuction(conn, auction);
            new PaymentService().processPayment(conn, auction);

            if (auction.getHighestBidder() != null) {
                bidderBalance = UserDAO.getBalance(auction.getHighestBidder().getId(), conn);
                sellerBalance = UserDAO.getBalance(auction.getSeller().getId(), conn);
            }
        }

        AUCTION_CACHE.remove(auctionId);

        // Phát sóng kết quả đến tất cả cửa sổ chi tiết đang mở
        String itemName = auction.getItem() != null ? auction.getItem().getName() : "Unknown";
        int winnerId = auction.getHighestBidder() != null ? auction.getHighestBidder().getId() : -1;
        double finalPrice = auction.getCurrentPrice();
        AuctionNotifier.getInstance().notifyAuctionResult(
                auctionId, itemName, winnerId, finalPrice, bidderBalance, sellerBalance);

        Map<String, Object> resp = ok();
        resp.put("winnerId", winnerId >= 0 ? winnerId : null);
        resp.put("finalPrice", finalPrice);
        resp.put("bidderNewBalance", bidderBalance >= 0 ? bidderBalance : null);
        resp.put("sellerNewBalance", sellerBalance >= 0 ? sellerBalance : null);
        sendJson(ex, 200, resp);
    }


    private void handleAutoBid(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int auctionId = body.get("auctionId").getAsInt();
        int bidderId = body.get("bidderId").getAsInt();
        double maxBid = body.get("maxBid").getAsDouble();
        double increment = body.get("increment").getAsDouble();
        Auction auction = getCachedAuction(auctionId);
        if (auction == null) {
            sendJson(ex, 200, err("Auction not found"));
            return;
        }
        Bidder bidder = new Bidder();
        bidder.setId(bidderId);

        // Giu lock de dam bao registerAutoBid + processAutoBids la atomic, tranh race condition
        auction.getLock().lock();
        double priceBeforeAutoBid = auction.getCurrentPrice();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                AdvancedAuctionService svc = new AdvancedAuctionService();
                svc.registerAutoBid(conn, auction, bidder, maxBid, increment);
                svc.processAutoBids(conn, auction);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } finally {
            auction.getLock().unlock();
        }

        // Neu proxy bid vua dat len gia moi, phat thong bao toi tat ca client dang mo
        if (auction.getCurrentPrice() > priceBeforeAutoBid && auction.getHighestBidder() != null) {
            AuctionNotifier.getInstance().notifyNewBid(
                    auctionId, auction.getCurrentPrice(), "AutoBid #" + auction.getHighestBidder().getId());
        }

        sendJson(ex, 200, ok());
    }


    private Auction getCachedAuction(int id) {
        return AUCTION_CACHE.computeIfAbsent(id, k -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                return new AuctionService().getById(conn, k);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
