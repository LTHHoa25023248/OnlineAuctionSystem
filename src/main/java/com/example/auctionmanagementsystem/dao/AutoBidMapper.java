package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AutoBid;
import com.example.auctionmanagementsystem.model.Bidder;
import java.sql.ResultSet;

public class AutoBidMapper {
  public static AutoBid mapRow(ResultSet rs) throws Exception {
    AutoBid autoBid = new AutoBid();
    autoBid.setId(rs.getInt("id"));
    autoBid.setMaxBid(rs.getDouble("max_bid"));
    autoBid.setIncrement(rs.getDouble("increment"));
    autoBid.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    // chi set id cho auction
    Auction auction = new Auction();
    auction.setId(rs.getInt("auction_id"));
    autoBid.setAuction(auction);
    if (!rs.wasNull()) {
      Bidder bidder = new Bidder();
      bidder.setId(rs.getInt("bidder_id"));
      autoBid.setBidder(bidder);
    }
    return autoBid;
  }

}
