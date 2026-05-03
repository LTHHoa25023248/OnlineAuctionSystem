package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.model.Auction;

import java.time.Duration;
import java.time.LocalDateTime;

public class AntiSnipingService {
    public void applyAntiSnipping(Auction auction){
        //Tinh thoi gian con lai cuoc dau gia
        long secondLeft= Duration.between(LocalDateTime.now(),auction.getEndTime()).getSeconds();
        //Neu gan het gio thi gia han them
        if(secondLeft<=10){
            auction.setEndTime(auction.getEndTime().plusSeconds(60));
        }
    }
}
