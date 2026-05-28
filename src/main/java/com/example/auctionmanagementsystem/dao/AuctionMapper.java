package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionMapper {

  public static Auction mapRow(ResultSet rs) throws SQLException {
    Auction auction = new Auction();
    auction.setId(rs.getInt("id"));
    auction.setCurrentPrice(rs.getDouble("current_price"));
    auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));
    auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
    auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());

    // 1. Map Item (Chỉ set ID bằng một đối tượng giả/proxy)
    // Lưu ý: Không khởi tạo ItemDAO ở đây. Để Factory hoặc subclass tạo đối tượng nếu cần,
    // hoặc tạm thời dùng một concrete class nếu Item là abstract (ví dụ: tạo một phương thức tĩnh trong ItemFactory để trả về stub).
    // Dưới đây giả định bạn có một cách tạo stub hoặc sử dụng subclass mặc định.
    int itemId = rs.getInt("item_id");
    if (!rs.wasNull()) {
      // Nếu Item là Abstract, bạn nên có cơ chế tạo Dummy Object (VD: ItemFactory.createDummyItem(itemId))
      // Ở đây tôi ví dụ tạo qua Factory để giữ ID.
      Item item = ItemFactory.createDummyItem(itemId);
      auction.setItem(item);
    }

    // 2. Map Seller (Chỉ set ID)
    int sellerId = rs.getInt("seller_id");
    if (!rs.wasNull()) {
      Seller seller = new Seller();
      seller.setId(sellerId);
      auction.setSeller(seller);
    }

    // 3. Map Highest Bidder (Chỉ set ID)
    int bidderId = rs.getInt("highest_bidder_id");
    if (!rs.wasNull()) {
      Bidder bidder = new Bidder();
      bidder.setId(bidderId);
      auction.setHighestBidder(bidder);
    }

    return auction;
  }
}
