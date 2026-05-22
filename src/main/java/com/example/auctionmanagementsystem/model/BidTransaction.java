package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidTransaction extends Entity {
  private User bidder;
  //auction nao
  private Auction auction;
  //so tien bid
  private double amount;
  private LocalDateTime time;

  public BidTransaction(Auction auction,User bidder, double amount) {
    this.auction=auction;
    this.bidder= bidder;
    this.amount = amount;
    this.time = LocalDateTime.now();
  }
  public BidTransaction(){};

  // sort,compare,.. time
  public LocalDateTime getTime() {
    return time;
  }
  public Auction getAuction(){return auction;}

  public User getBidder() {
    return bidder;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(final double amount) {
    this.amount = amount;
  }

  public void setBidder(final User bidder) {
    this.bidder = bidder;
  }

  public void setAuction(final Auction auction) {
    this.auction = auction;
  }

  public void setTime(final LocalDateTime time) {
    this.time = time;
  }

  public String getFormattedTime() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    return time.format(formatter);
  }
}
