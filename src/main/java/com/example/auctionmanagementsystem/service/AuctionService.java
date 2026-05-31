package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.dao.AuctionDAO;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionService {
    private final AuctionDAO auctionDao;

    public AuctionService() {
        this.auctionDao = new AuctionDAO();
    }

    // UNUSED — constructor dự phòng cho unit test (mock DAO), không được gọi trong app
    public AuctionService(AuctionDAO auctionDao) {
        this.auctionDao = auctionDao;
    }
    //tao phien dau gia
    public void createAuction(Connection connect, Auction auction){
        try{
            //kiem tra du lieu
            if(auction.getItem()==null){
                throw new IllegalArgumentException("Item cannot be null");
            }
            if(auction.getSeller()==null){
                throw new IllegalArgumentException("Seller cannot be null");
            }
            // set trang thoi cho admin duyet
            auction.setStatus(AuctionStatus.PENDING);
            auctionDao.insert(auction,connect);
          
        }catch(Exception e){
            throw new RuntimeException("Create auction failed",e);
        }
    }
    // UNUSED — startAuction() không được gọi ở đâu.
    // Approve xử lý trực tiếp bằng SQL trong AuctionHandler.handleApprove()
    public void startAuction(Connection connect, Auction auction) {
        try {
            //set trang thai duyet da  moi cho mo
            if (auction.getStatus() != AuctionStatus.PENDING) {
                throw new IllegalStateException("Auction must be PENDING to start");
            }
            //set trang thai, time, update data
            auction.setStatus(AuctionStatus.OPEN);
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

    // UNUSED — cancelAuction() không được gọi ở đâu trong app
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

    // UNUSED — getAll() không được gọi. AuctionHandler.handleList() query thẳng SQL
    public List<Auction> getAll(Connection connect) {
        try {
            return auctionDao.selectAll(connect);
        } catch (Exception e) {
            throw new RuntimeException("Get all auctions failed", e);
        }
    }

    // UNUSED — getActiveAuctions() không được gọi.
    // AuctionScheduler gọi auctionDao.selectOpenAuctions() trực tiếp, bỏ qua method này
    public List<Auction> getActiveAuctions(Connection connect) {
        try {
            return auctionDao.selectOpenAuctions(connect);
        } catch (Exception e) {
            throw new RuntimeException("Get active auctions failed", e);
        }
    }

    // UNUSED — updateAuction() không được gọi từ bên ngoài.
    // Các method nội bộ (endAuction, cancelAuction...) gọi auctionDao.update() trực tiếp
    public void updateAuction(Connection connect, Auction auction) {
        try {
            auctionDao.update(auction, connect);
        } catch (Exception e) {
            throw new RuntimeException("Update auction failed", e);
        }
    }
    // UNUSED — deleteAuction() không được gọi. ItemHandler.handleDelete() dùng AuctionDAO trực tiếp
    public void deleteAuction(Connection connect, int id) {
        try {
            auctionDao.delete(id, connect);
        } catch (Exception e) {
            throw new RuntimeException("Delete auction failed", e);
        }
    }

   
     // UNUSED — getPendingAuctions() không được gọi. AdminHandler query SQL trực tiếp
    public List<Auction> getPendingAuctions(Connection connect) {
        try {
            return auctionDao.selectPendingAuctions(connect);
        } catch (Exception e) {
            throw new RuntimeException("Get pending auctions failed", e);
        }
    }
    // UNUSED — approveAuction() không được gọi.
    // AuctionHandler.handleApprove() chạy SQL UPDATE trực tiếp, không qua Service
    public void approveAuction(Connection connect, Auction auction) {
        try {
            // Chi duyet neu dang o trang thai PENDING
            if (auction.getStatus() != AuctionStatus.PENDING) {
                throw new IllegalStateException("Can only approve auctions in PENDING status");
            }
            auction.setStatus(AuctionStatus.OPEN);
            // xoa ly do tu choi neu truoc do da tung b admin tu choiii
            auction.setRejectReason(null); 
            auctionDao.update(auction, connect);
        } catch (Exception e) {
            throw new RuntimeException("Approve auction failed", e);
        }
    }

    // UNUSED — rejectAuction() không được gọi.
    // AuctionHandler.handleReject() chạy SQL UPDATE trực tiếp, không qua Service
    public void rejectAuction(Connection connect, Auction auction, String reason) {
        try {
            //chi duoc tu choi item dang o trang thai cho admin phe duyet -PENDING
            if (auction.getStatus() != AuctionStatus.PENDING) {
                throw new IllegalStateException("Can only reject auctions in PENDING status");
            }
            // bat buoc dua ra ly do tu choi
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Reject reason cannot be empty");
            }
            auction.setStatus(AuctionStatus.REJECTED);
            auction.setRejectReason(reason.trim());
            auctionDao.update(auction, connect);
        } catch (Exception e) {
            throw new RuntimeException("Reject auction failed", e);
        }
    }


}
