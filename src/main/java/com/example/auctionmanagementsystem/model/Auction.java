package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Auction {
    private String idAuction;
    private Item item;
    private double currentPrice;
    private User highestBidder;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<BidTransaction> historyBid=new ArrayList<>();

    public double getCurrentPrice(){
        return currentPrice;
    }
    public User getHighestBidder(){
        return highestBidder;
    }
    public AuctionStatus getStatus(){
        return status;
    }

    public List<BidTransaction> getHistoryBid() {
        return historyBid;
    }
    public String getIdAuction(){
        return idAuction;
    }
    public Item getItem(){
        return item;
    }
}
