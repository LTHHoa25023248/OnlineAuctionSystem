package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.exception.InsufficientBalanceException;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;

import java.sql.Connection;

public class PaymentService {
    private final AuctionDAO auctionDAO = new AuctionDAO();
    public void processPayment(Connection connect, Auction auction) throws Exception {
        //kiem tra trang thai xem auction finished chua
        if (auction.getStatus() != AuctionStatus.FINISHED) {
            throw new IllegalStateException("Payment can only be processed for FINISHED auctions.");
        }
        connect.setAutoCommit(false);
        try {
            //kiem tra xem ai thang
            if (auction.getHighestBidder() == null) {
                connect.commit();
                return;
            }

            double price     = auction.getCurrentPrice();
            int    bidderId  = auction.getHighestBidder().getId();
            int    sellerId  = auction.getSeller().getId();

            double bidderBalance = UserDAO.getBalance(bidderId, connect);
            //kiem tra so du bidder co du khong
            if (bidderBalance < price) {
                throw new InsufficientBalanceException(price, bidderBalance);
            }

            double sellerBalance = UserDAO.getBalance(sellerId, connect);
           // tru tien bidder, cong tien cho seller
            UserDAO.updateBalance(bidderId, bidderBalance - price, connect);
            UserDAO.updateBalance(sellerId, sellerBalance + price, connect);
            //set lai ve trnang thai da PAID
            auction.setStatus(AuctionStatus.PAID);
            auctionDAO.update(auction, connect);
            //luu ve lai du lieu
            connect.commit();
        } catch (Exception e) {
            connect.rollback();
            throw e;
        } finally {
            connect.setAutoCommit(true);
        }
    }
}
