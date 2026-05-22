package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BidTransactionMapper {
  public static BidTransaction mapRow(ResultSet rs) throws SQLException {
    BidTransaction bid = new BidTransaction();
    bid.setId(rs.getInt("id"));
    bid.setAmount(rs.getDouble("amount"));
    bid.setTime(rs.getTimestamp("bid_time").toLocalDateTime());
    // acution setId thoi
    Auction auction = new Auction();
    auction.setId(rs.getInt("auction_id"));
    bid.setAuction(auction);
    // bidder cung chi setId
    if (!rs.wasNull()) {
      Bidder bidder = new Bidder();
      bidder.setId(rs.getInt("bidder_id"));
      bid.setBidder(bidder);
    }
    return bid;
  }
}
