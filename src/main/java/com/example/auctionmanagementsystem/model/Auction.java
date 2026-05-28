package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {

  private Item item;
  private int id;
  private int bids;
  private int daysLeft;
  private double currentPrice;
  private User highestBidder;
  private AuctionStatus status;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Seller seller;
  private List<BidTransaction> historyBid = new ArrayList<>();
  private List<AutoBid> hisAutoBid = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private String rejectReason;

  public double getCurrentPrice() {
    return currentPrice;
  }

  public User getHighestBidder() {
    return highestBidder;
  }

  public AuctionStatus getStatus() {
    return status;
  }

  public List<BidTransaction> getHistoryBid() {
    return historyBid;
  }

  public Item getItem() {
    return item;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public List<AutoBid> getHisAutoBid() {
    return hisAutoBid;
  }

  public ReentrantLock getLock() {
    return lock;
  }

  public Seller getSeller() {
    return seller;
  }

  public void setSeller(Seller seller) {
    this.seller = seller;
  }

  public void setItem(final Item item) {
    this.item = item;
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
    return status == AuctionStatus.RUNNING || status == AuctionStatus.OPEN;
  }

  public void addBid(BidTransaction bid) {
    historyBid.add(bid);
  }

  public void addAutoBid(AutoBid autoBid) {
    hisAutoBid.add(autoBid);
  }

  public String getRejectReason() {
    return rejectReason;
  }

  public void setRejectReason(String rejectReason) {
    this.rejectReason = rejectReason;
  }

  // Tao constructor rong de phan DAO set duoc
  public Auction() {}

  public Auction(Item item, Seller seller, double currentPrice, AuctionStatus status,
      LocalDateTime startTime, LocalDateTime endTime) {
    this.item = item;
    this.seller = seller;
    this.currentPrice = currentPrice;
    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;
  }

}
