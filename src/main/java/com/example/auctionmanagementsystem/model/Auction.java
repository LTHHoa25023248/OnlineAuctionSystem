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
    //tao khoa dam bao viec dau gia, tranh viec doc va ghi chong len nhau
    private final ReentrantLock lock=new ReentrantLock();
    public Auction(String idAuction, Item item, LocalDateTime startTime, LocalDateTime endTime){
        this.idAuction=idAuction;
        this.currentPrice=currentPrice;
        this.startTime=startTime;
        this.endTime=endTime;
        this.status=AuctionStatus.OPEN;

    }
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

    public void placeBid(User user, double amount){
        //tranh 2 nguoi bid 1 luc
        lock.lock();
        try{
            //xem phien dau co dang hoat dong hay ko
            if (status != AuctionStatus.RUNNING) {
                throw new IllegalStateException("Auction is not running");
            }
            //check time hien tai da qua gio dau gia chua
            if (LocalDateTime.now().isAfter(endTime)){
                status= AuctionStatus.FINISHED;
                throw new IllegalStateException("Auction already ended");
            }
            //so sanh gia dat voi gia hien tai cua san pham
            if (amount<=currentPrice){
                throw new IllegalArgumentException("Bid must be higher than current price");

            }
            currentPrice=amount;
            highestBidder=user;
            historyBid.add(new BidTransaction(user,amount));
        }
        finally {
            lock.unlock();
        }

    }
    //check xem phien het han chua
    public void finishAuction() {
        if (status == AuctionStatus.FINISHED)
            return;

        if (LocalDateTime.now().isAfter(endTime)) {
            status = AuctionStatus.FINISHED;
        }
    }



}
