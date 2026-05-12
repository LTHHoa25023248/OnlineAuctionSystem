package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidTransaction {
    private User user;
    private double amount;
    private LocalDateTime time;
    public BidTransaction(User user,double amount){
        this.user=user;
        this.amount=amount;
        this.time=LocalDateTime.now();
    }
    //sort,compare,.. time
    public LocalDateTime getTime(){
        return time;
    }
    public User user(){
        return user;
    }
    public double amount(){
        return amount;
    }
    public String getFormattedTime(){
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return time.format(formatter);
    }
}
