package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.dao.AuctionDAO;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionService {
    private final AuctionDAO auctionDao=new AuctionDAO();
    //tao phien dau gia
    public void creatAuction(Connection connect, Auction auction){
        try{
            //kiem tra du lieu
            if(auction.getItem()==null){
                throw new IllegalArgumentException("Item cannot be null");
            }
            if(auction.getSeller()==null){
                throw new IllegalArgumentException("Seller cannot be null");
            }
            //set trang thai ban dau
            auction.setStatus(AuctionStatus.OPEN);
            auctionDao.insert(auction,connect);
          
        }catch(Exception e){
            throw new RuntimeException("Create auction failed",e);
        }
    }
    // mo phien dau gia
    public void startAuction(Connection connect, Auction auction) {
        try {
            if (auction.getStatus() != AuctionStatus.OPEN) {
                throw new IllegalStateException("Auction must be OPEN to start");
            }
            auction.setStatus(AuctionStatus.RUNNING);
            auction.setStartTime(LocalDateTime.now());
            auctionDao.update(auction, connect);

        } catch (Exception e) {
            throw new RuntimeException("Start auction failed", e);
        }
    }

    public void endAuction(Connection connect, Auction auction) {
        try {
            if (auction.getStatus() == AuctionStatus.FINISHED) {
                return;
            }
            auction.setStatus(AuctionStatus.FINISHED);
            auction.setEndTime(LocalDateTime.now());
            auctionDao.update(auction, connect);

        } catch (Exception e) {
            throw new RuntimeException("End auction failed", e);
        }
    }

    public void cancelAuction(Connection connect, Auction auction) {

    try {
            if (auction.getStatus() == AuctionStatus.FINISHED) {
                throw new IllegalStateException("Cannot cancel finished auction");
            }

            auction.setStatus(AuctionStatus.CANCELED);
            auctionDao.update(auction, connect);

        } catch (Exception e) {
            throw new RuntimeException("Cancel auction failed", e);
        }
    }
    public Auction getById(Connection connect, int id) {
        try {
            return auctionDao.selectById(id, connect);

        } catch (Exception e) {
            throw new RuntimeException("Get auction failed", e);
        }
    }

    public List<Auction> getAll(Connection connect) {
        try {
            return auctionDao.selectAll(connect);
        } catch (Exception e) {
            throw new RuntimeException("Get all auctions failed", e);
        }
    }

    public List<Auction> getActiveAuctions(Connection connect) {
        try {
            return auctionDao.selectOpenAuctions(connect);
        } catch (Exception e) {
            throw new RuntimeException("Get active auctions failed", e);
        }
    }

    public void updateAuction(Connection connect, Auction auction) {
        try {
            auctionDao.update(auction, connect);
        } catch (Exception e) {
            throw new RuntimeException("Update auction failed", e);
        }
    }
    public void deleteAuction(Connection connect, int id) {
        try {
            auctionDao.delete(id, connect);
        } catch (Exception e) {
            throw new RuntimeException("Delete auction failed", e);
        }
    }

}
