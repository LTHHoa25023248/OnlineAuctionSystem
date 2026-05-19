package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.*;
import java.sql.ResultSet;
import java.sql.SQLException;
public class AuctionMapper {

   public static Auction mapRow(ResultSet rs) throws SQLException{
    Auction auction=new Auction();
                  auction.setId(rs.getInt("id"));
                  auction.setCurrentPrice(rs.getDouble("current_price"));
                  auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));
                  auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                  auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
    //Set item-> chi set id, khong join vao full ohject. tranh viec query nang
    int itemId=rs.getInt("item_id");
    //Item dang de abstract, nho ItemDao tao doi tuong-> boi trong itemDao co selectById co factory, return ve item
    ItemDAO itemDao=new ItemDAO();
    Item item=itemDao.selectById(rs.getInt("item_id"));
    auction.setItem(item);
    //set Seller cung chi set id
    int sellerId =rs.getInt("seller_id");
    Seller seller=new Seller();
    seller.setId(sellerId);
    auction.setSeller(seller);
    // set highest bidder
    int bidderId=rs.getInt("highest_bidder_id");
    //rs.wasNull() kiem tra neu DB tra ve Null
       if(!rs.wasNull()){
        Bidder bidder=new Bidder();
        bidder.setId(bidderId);
        auction.setHighestBidder(bidder);
    }
       return auction;
}
}
