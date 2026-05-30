package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.model.ItemFactory;
import com.example.auctionmanagementsystem.model.Seller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chuyển JSON trả về từ "/auction/detail" thành các đối tượng domain.
 *
 * Tách ra khỏi AuctionDetailController để controller không phải ôm phần đọc JSON dài dòng;
 * đây là logic thuần (không đụng JavaFX) nên dễ test và tái sử dụng.
 */
public final class AuctionDetailMapper {

    private AuctionDetailMapper() {}

    /** Gói kết quả parse: phiên đấu giá (đã gắn item) + danh sách lượt đặt giá. */
    public static final class Result {
        public final Auction auction;
        public final Item item;
        public final List<BidTransaction> bids;
        Result(Auction auction, Item item, List<BidTransaction> bids) {
            this.auction = auction;
            this.item = item;
            this.bids = bids;
        }
    }

    public static Result parse(JsonObject data) {
        Auction auction = new Auction();
        auction.setId(data.get("id").getAsInt());
        auction.setCurrentPrice(data.get("currentPrice").getAsDouble());
        auction.setStatus(AuctionStatus.valueOf(data.get("status").getAsString()));
        if (!data.get("startTime").isJsonNull())
            auction.setStartTime(LocalDateTime.parse(data.get("startTime").getAsString()));
        if (!data.get("endTime").isJsonNull())
            auction.setEndTime(LocalDateTime.parse(data.get("endTime").getAsString()));
        if (!data.get("highestBidderId").isJsonNull()) {
            Bidder hb = new Bidder();
            hb.setId(data.get("highestBidderId").getAsInt());
            auction.setHighestBidder(hb);
        }
        Seller seller = new Seller();
        seller.setId(data.get("sellerId").getAsInt());
        auction.setSeller(seller);
        if (!data.get("rejectReason").isJsonNull())
            auction.setRejectReason(data.get("rejectReason").getAsString());

        String itemType = data.get("itemType").getAsString();
        String itemName = data.get("itemName").isJsonNull() ? "" : data.get("itemName").getAsString();
        String itemDesc = data.get("itemDescription").isJsonNull()
                ? "" : data.get("itemDescription").getAsString();
        Item item = ItemFactory.createItem(itemType, itemName, itemDesc,
                auction.getCurrentPrice(), Collections.emptyMap());
        item.setId(data.get("itemId").getAsInt());
        if (!data.get("itemImagePath").isJsonNull())
            item.setImagePath(data.get("itemImagePath").getAsString());
        auction.setItem(item);

        List<BidTransaction> bids = new ArrayList<>();
        JsonArray bidsArr = data.getAsJsonArray("bids");
        if (bidsArr != null) {
            for (JsonElement el : bidsArr) {
                JsonObject bo = el.getAsJsonObject();
                BidTransaction bt = new BidTransaction();
                bt.setId(bo.get("id").getAsInt());
                bt.setAmount(bo.get("amount").getAsDouble());
                bt.setTime(!bo.get("bidTime").isJsonNull()
                        ? LocalDateTime.parse(bo.get("bidTime").getAsString()) : LocalDateTime.now());
                bids.add(bt);
            }
        }

        return new Result(auction, item, bids);
    }
}
