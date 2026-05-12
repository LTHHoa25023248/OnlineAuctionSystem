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
    private List<AutoBid> hisAutoBid=new ArrayList<>();
    private final ReentrantLock lock=new ReentrantLock();
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
    public LocalDateTime getStartTime(){
        return startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<AutoBid> getHisAutoBid(){
        return hisAutoBid;
    }
    public ReentrantLock getLock() {
        return lock;
    }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setHighestBidder(User highestBidder) {
        this.highestBidder = highestBidder;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public boolean isOpen() {
        return status == AuctionStatus.RUNNING;
    }

    public void addBid(BidTransaction bid) {
        historyBid.add(bid);
    }

    public void addAutoBid(AutoBid autoBid) {
        hisAutoBid.add(autoBid);
    }

}
