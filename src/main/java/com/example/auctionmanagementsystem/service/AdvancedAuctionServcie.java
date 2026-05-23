package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AutoBid;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.dao.AutoBidDAO;
import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.BidTransactionDAO;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class AdvancedAuctionServcie {
    private final AutoBidDAO autoBidDao=new AutoBidDAO();
    private final AuctionDAO auctionDao=new AuctionDAO();
    private final BidTransactionDAO bidDao=new BidTransactionDAO();
    //dang ky dau gia tu dong
    public void registerAutoBid(Connection connect, Auction auction, Bidder bidder, double maxBid, double increment ){
        // tao cac doi tuong dau gia tu dong
        AutoBid autoBid = new AutoBid(auction,bidder,maxBid,increment);
       //luu du lieu
        autoBidDao.insert(connect,autoBid);
    }

    //Qua trinh luu tu dong
    public void processAutoBids(Connection connect, Auction auction){
        // load het tat ca cac autoBid
        List<AutoBid> autoBids=autoBidDao.selectByAuctionId(connect, auction.getId());
        // ai dang ky truoc thi uu tien truoc, sap xep theo thoi gian dang ky
        PriorityQueue<AutoBid> queue=new PriorityQueue<>(Comparator.comparing(AutoBid::getCreatedAt));
        queue.addAll(autoBids);
        boolean updated=true;
        //lap cho dne khi ko ai bid duoc nua
        while (updated){
            updated=false;
            for (AutoBid autoBid: queue){
                Bidder bidder=autoBid.getBidder();
                //bo qua neu dang la nguoi dan dau
                if(auction.getHighestBidder()!=null && auction.getHighestBidder().getId()==bidder.getId()){
                    continue;
                }
                // tinh gia bid tiep theo
                double nextBid=auction.getCurrentPrice()+autoBid.getIncrement();
                //kiem tra xem co vuot qua gia toi da minh dua ra khong
                if(nextBid>autoBid.getMaxBid()){
                    continue;
                }
                //update auction
                auction.setCurrentPrice(nextBid);
                auction.setHighestBidder(bidder);
                //luu lich su bid
                BidTransaction bid =new BidTransaction();
                bid.setAuction(auction);
                bid.setBidder(bidder);
                bid.setAmount(nextBid);
                bid.setTime(LocalDateTime.now());
                bidDao.insert(connect,bid);
                //update du lieu cua auction trong DB
                
                auctionDao.update(auction,connect);
                updated=true;
               
            }
        }

    }
    //gia han thoi gian
    public void applyAntiSniping(Connection connect, Auction auction){
        long secondsLeft= Duration.between(LocalDateTime.now(),auction.getEndTime()).getSeconds();
        //con 10s cuoi, gia han them 60s
        if(secondsLeft<=10){
            auction.setEndTime(auction.getEndTime().plusSeconds(60));
            //update du lieu auction
             auctionDao.update(auction,connect);

        }
    }
}
