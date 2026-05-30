package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.exception.InsufficientBalanceException;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentService {
    //loi nhuan hoa hong admin huong 10% tu san pham ban duoc
    private static final double ADMIN_COMMISSION_RATE = 0.10;
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
            //admin nhan
            double commission    = price * ADMIN_COMMISSION_RATE;
            //seller thuc nhan chi la 90%
            double sellerProceeds = price * (1.0 - ADMIN_COMMISSION_RATE);

            int    adminId      = getAdminId(connect);
            double adminBalance = UserDAO.getBalance(adminId, connect);
            double sellerBalance = UserDAO.getBalance(sellerId, connect);

            UserDAO.updateBalance(bidderId, bidderBalance - price, connect);
            UserDAO.updateBalance(sellerId, sellerBalance + sellerProceeds, connect);  // [CHANGED] 90%
            UserDAO.updateBalance(adminId,  adminBalance  + commission,     connect);  // [CHANGED] 10%
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
    private int getAdminId(Connection connect) throws Exception {
        String sql = "SELECT id FROM users WHERE role='ADMIN' LIMIT 1";
        try (PreparedStatement ps = connect.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new IllegalStateException("No admin user found in the database");
    }
}
