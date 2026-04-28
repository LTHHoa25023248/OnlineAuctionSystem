package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;

public class AutoBid {
    //Ai dang dat gia
    private User user;
    //Gia cao nhat ma user chiu chi
    private double maxBid;
    //
    private double increment;
    private LocalDateTime createdAt;
    public AutoBid(User user, double maxBid, double increment){
        this.user=user;
        this.maxBid=maxBid;
        this.increment=increment;
        this.createdAt=LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public double getIncrement() {
        return increment;
    }

    public double getMaxBid() {
        return maxBid;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;

    }
}
