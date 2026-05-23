package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.exception.AuctionClosedException;

import com.example.auctionmanagementsystem.exception.InvalidBidException;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Bidder;
import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.BidTransactionDAO;

import java.sql.Connection;
import java.time.LocalDateTime;


public class BiddingService {
    private final AuctionDAO auctionDao=new AuctionDAO();
    private final BidTransactionDAO bidDao=new BidTransactionDAO();
    private final AdvancedAuctionServcie advancedServcie=new AdvancedAuctionServcie();
    //logic dau gia
    public void placeBid(Auction auction,Bidder bidder,double amount){
        //lock de trang nhieu nguoi bid cung luc, goi tu auction
        auction.getLock().lock();
        // cho ket noi = null de thuc hien rollback
        Connection connect=null;
        try{
            //mo ket noi
            connect=new DatabaseConnection().getConnection();
            //gom lai +luu du lieu tam thoi, chua ghi xuong database
            connect.setAutoCommit(false);
            //kiem tra trang thai xem phien dau gia co mo khong
            if(!auction.isOpen()){
                throw new AuctionClosedException();
            }
            // kiem tra thoi gian dau gia, xem ket thuc chua
            if(LocalDateTime.now().isAfter(auction.getEndTime())){
                throw new AuctionClosedException();
            }
            //kiem tra gia bid hop le
            if (amount<= auction.getCurrentPrice()){
                throw new InvalidBidException(amount,auction.getCurrentPrice());
            }

            // update phien dau
            // Gia dau moi nhat, nguoi dau gia cao nhat sau cap nhap
            auction.setCurrentPrice(amount);
            auction.setHighestBidder(bidder);
            // Luu Bidtransaction
            BidTransaction bid =new BidTransaction();
            bid.setAuction(auction);
            bid.setBidder(bidder);
            bid.setAmount(amount);
            bid.setTime(LocalDateTime.now());
            bidDao.insert(connect,bid);
            //update Auction trong DB
            auctionDao.update(auction,connect);
            //qua trinh tu dau gia
            advancedServcie.processAutoBids(connect,auction);
            //gia han thoi gian
            advancedServcie.applyAntiSniping(connect,auction);
            //tat ca o tren deu thanh cong thi luu du lieu vao database
            connect.commit();


        }catch(Exception e){
            //neu co van de thi rollback, du lieu duoc quay tro ve luc chua thay doi
            try{
                if(connect!=null){
                    connect.rollback();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
            throw new RuntimeException("Place bid failed",e);
        }finally{
            //dong ket noi
            try{
                if(connect!=null){
                    connect.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            //mo lock de nguoi kha di vao dau gia
            auction.getLock().unlock();
        }
    }

}
