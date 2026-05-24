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

    int itemId = rs.getInt("item_id");
    if (!rs.wasNull()) {
     //tao item qua ItemFactory
      Item item = ItemFactory.createDummyItem(itemId);
      auction.setItem(item);
    }

    //chi set id
    int sellerId = rs.getInt("seller_id");
    if (!rs.wasNull()) {
      Seller seller = new Seller();
      seller.setId(sellerId);
      auction.setSeller(seller);
    }
   //chi set id 
    int bidderId = rs.getInt("highest_bidder_id");
    if (!rs.wasNull()) {
      Bidder bidder = new Bidder();
      bidder.setId(bidderId);
      auction.setHighestBidder(bidder);
    }
    // doc ly do admin tu choi
    String rejectReason = rs.getString("reject_reason");
    if (!rs.wasNull()) {
      // set neu cot khong null
      auction.setRejectReason(rejectReason);
    }
   

    return auction;
  }
}
