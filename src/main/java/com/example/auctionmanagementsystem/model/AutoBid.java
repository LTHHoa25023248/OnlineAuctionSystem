package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;

public class AutoBid extends Entity {
  private Auction auction;
  // Ai dang dat gia
  private Bidder bidder;
  // Gia cao nhat ma user chiu chi
  private double maxBid;
  // Gia tien tu dong tang len sau moi lan dat
  private double increment;
  // Thoi gian autobid
  // Cho biet thoi gian de uu tien thu tu nguoi dat gia
  private LocalDateTime createdAt;

  public AutoBid() {
    this.createdAt = LocalDateTime.now();
  }

  public AutoBid(Auction auction, Bidder bidder, double maxBid, double increment) {
    this.auction = auction;
    this.bidder = bidder;
    this.maxBid = maxBid;
    this.increment = increment;
    this.createdAt = LocalDateTime.now();
  }

  public Auction getAuction() {
    return auction;
  }

  public Bidder getBidder() {
    return bidder;
  }

  public double getIncrement() {
    return increment;
  }

  public double getMaxBid() {
    return maxBid;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;

  }

  public void setAuction(Auction auction) {
    this.auction = auction;
  }

  public void setBidder(Bidder bidder) {
    this.bidder = bidder;
  }

  public void setMaxBid(double maxBid) {
    this.maxBid = maxBid;
  }

  public void setIncrement(double increment) {
    this.increment = increment;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
