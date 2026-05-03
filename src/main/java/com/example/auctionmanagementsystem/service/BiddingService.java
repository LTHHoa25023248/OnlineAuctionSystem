package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.exception.AuctionClosedException;
import com.example.auctionmanagementsystem.exception.AuctionStatusException;
import com.example.auctionmanagementsystem.exception.InvalidBidException;
import com.example.auctionmanagementsystem.exception.SellerBiddingOwnItemException;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.User;

import java.time.LocalDateTime;

public class BiddingService {
    private final AutoBidService autoBidService =new AutoBidService();
    private final AntiSnipingService antiSnipingService=new AntiSnipingService();
    public void placeBid(Auction auction, User user, double amount){
        //Khong dat cung luc
        auction.getLock().lock();
        try{
            //kiem tra trang thai phien, ko mo thi ko cho dau gia
            if(!auction.isOpen()){
                throw new AuctionClosedException();
            }
            if (auction.getStatus() == AuctionStatus.FINISHED ||
                    auction.getStatus() == AuctionStatus.CANCELED) {
                throw new AuctionStatusException(auction.getStatus().name());
            }
            //Kiem tra thoi gian, neu thoi gian hien tai vuot qua tg ket thuc-> ko cho dau gia nua
            if (LocalDateTime.now().isAfter(auction.getEndTime())) {
                throw new AuctionClosedException();
            }
            //gia phai cao hon gia hien tai
            if (amount <= auction.getCurrentPrice()) {
                throw new InvalidBidException(amount, auction.getCurrentPrice());
            }
            //cap nhat gia hien tai, nguoi dang dan dau
            auction.setHighestBidder(user);
            //luu ls dau gia
            auction.addBid(new BidTransaction(user, amount));
            //Neu co nguoi dung dat autobid->he thong tu dau gia thay
            autoBidService.processAutoBids(auction);
            //neu co nguoi dat gia can cuoi gio--> gia han thoi gian
            antiSnipingService.applyAntiSnipping(auction);


        }finally {
            auction.getLock().unlock();
        }
    }
}
