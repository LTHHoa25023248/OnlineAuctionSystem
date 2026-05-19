package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionDAO {
    public int insert(BidTransaction bid) {
        Connection connect = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connect = new DatabaseConnection().getConnection();
            connect.setAutoCommit(false);
            String sql = "INSERT INTO bid_transaction (auction_id, bidder_id, amount, bid_time) VALUES(?,?,?,?)";
            ps = connect.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bid.getAuction().getId());
            ps.setInt(2, bid.getBidder().getId());
            ps.setDouble(3, bid.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(bid.getTime()));
            ps.executeUpdate();
            //lay id sinh ra tu DB roi gan lai
            rs = ps.getGeneratedKeys();
            int idBid;
            if (rs.next()) {
                idBid = rs.getInt(1);
                bid.setId(idBid);
            } else {
                throw new RuntimeException("Cannot get generated bid ID");


            }
            //luu du lieu
            connect.commit();
            return idBid;
        } catch (Exception e) {
            try {
                if (connect != null)
                    connect.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Insert bidtransaction failed", e);
        } finally {
            close(rs);
            close(ps);
            close(connect);
        }

    }

    //xem lich su dau gia cua 1 phien dau gia
    public List<BidTransaction> selectByAuctionId(int auctionId) {
        List<BidTransaction> list = new ArrayList<>();
        Connection connect = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connect = new DatabaseConnection().getConnection();
            //sql lay toan bo bid theo auction_id
            String sql = "SELECT * FROM bid_transaction WHERE auction_id=? ORDER BY bid_time ASC";
            ps = connect.prepareStatement(sql);
            ps.setInt(1, auctionId);
            //thuc hien lay du lieu
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(BidTransactionMapper.mapRow(rs));


            }
            return list;

        } catch (Exception e) {
            throw new RuntimeException("SelectByAuctionId failed", e);
        } finally {
            close(rs);
            close(ps);
            close(connect);
        }
    }

    //chon lich su dau gia tat ca auction cua 1 bidder
    public List<BidTransaction> selectByBidderId(int bidderId) {
        List<BidTransaction> list = new ArrayList<>();
        Connection connect = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connect = new DatabaseConnection().getConnection();
            String sql = "SELECT * FROM bid_transaction WHERE bidder_id=? ORDER BY bid_time DESC";
            ps = connect.prepareStatement(sql);
            //gan bidderId vao dau ?
            ps.setInt(1, bidderId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(BidTransactionMapper.mapRow(rs));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("SelectByBidderId failed", e);

        } finally {
            close(rs);
            close(ps);
            close(connect);
        }
    }

    //lay lich su dau gia gan day nhat
    public BidTransaction findLatestByAuctionId(int auctionId) {
        Connection connect = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connect = new DatabaseConnection().getConnection();
            String sql = "SELECT * FROM bid_transaction WHERE auction_id=? ORDER BY bid_time DESC LIMIT 1";
            ps = connect.prepareStatement(sql);
            ps.setInt(1, auctionId);
            rs=ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return BidTransactionMapper.mapRow(rs);
        } catch (Exception e) {
            throw new RuntimeException("findLastestByAuctionid failed", e);
        } finally {
            close(rs);
            close(ps);
            close(connect);
        }
    }



    private void close(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
