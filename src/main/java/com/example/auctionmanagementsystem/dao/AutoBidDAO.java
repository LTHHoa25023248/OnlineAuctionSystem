package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.AutoBid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AutoBidDAO {
    public int insert(Connection connect, AutoBid autoBid) {
            String sql = "INSERT INTO auto_bid (auction_id, bidder_id, max_bid, increment, created_at) VALUES (?, ?, ?, ?, ?)";
          //sau khi chen xong giu lai id tu dong sinh ra
            try (PreparedStatement ps = connect.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, autoBid.getAuction().getId());
                ps.setInt(2, autoBid.getBidder().getId());
                ps.setDouble(3, autoBid.getMaxBid());
                ps.setDouble(4, autoBid.getIncrement());
                ps.setTimestamp(5, Timestamp.valueOf(autoBid.getCreatedAt()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        autoBid.setId(id);
                        return id;
                    }
                }
                throw new RuntimeException("Cannot get generated AutoBid ID");

            } catch (Exception e) {
                throw new RuntimeException("Insert AutoBid failed", e);
            }
        }
        public List<AutoBid> selectByAuctionId(Connection connect, int auctionId) {
            List<AutoBid> list = new ArrayList<>();
            String sql = "SELECT * FROM auto_bid WHERE auction_id=? ORDER BY created_at ASC";
            try (PreparedStatement ps = connect.prepareStatement(sql)) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(AutoBidMapper.mapRow(rs));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Select AutoBid by auction failed", e);
            }
            return list;
        }
        public List<AutoBid> selectByBidderId(Connection connect, int bidderId) {
            List<AutoBid> list = new ArrayList<>();
            String sql = "SELECT * FROM auto_bid WHERE bidder_id=? ORDER BY created_at DESC";
            try (PreparedStatement ps = connect.prepareStatement(sql)) {
                ps.setInt(1, bidderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(AutoBidMapper.mapRow(rs));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Select AutoBid by bidder failed", e);
            }
            return list;
        }

        public int delete(Connection connect, int id) {
            String sql = "DELETE FROM auto_bid WHERE id=?";
            try (PreparedStatement ps = connect.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException("Delete AutoBid failed", e);
            }
        }

}
