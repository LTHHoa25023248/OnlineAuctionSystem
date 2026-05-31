package com.example.auctionmanagementsystem.dao;
import com.example.auctionmanagementsystem.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionDAO {
  public int insert(Connection connect, BidTransaction bid) {
    String sql =
        "INSERT INTO bid_transaction (auction_id, bidder_id, amount, bid_time) VALUES (?, ?, ?, ?)";
    // sau khi chen xong giu lai id tu dong sinh ra
    try (PreparedStatement ps = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, bid.getAuction().getId());
      ps.setInt(2, bid.getBidder().getId());
      ps.setDouble(3, bid.getAmount());
      ps.setTimestamp(4, Timestamp.valueOf(bid.getTime()));
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          int id = rs.getInt(1);
          bid.setId(id);
          return id;
        }
      }
      throw new RuntimeException("Cannot get generated bid id");

    } catch (Exception e) {
      throw new RuntimeException("Insert bid failed", e);
    }
  }

  public List<BidTransaction> selectByAuctionId(Connection connect, int auctionId) {
    String sql = "SELECT * FROM bid_transaction WHERE auction_id=? ORDER BY bid_time ASC";
    List<BidTransaction> list = new ArrayList<>();
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(BidTransactionMapper.mapRow(rs));
        }
      }
      return list;

    } catch (Exception e) {
      throw new RuntimeException("Select bids by auction failed", e);
    }
  }

  public List<BidTransaction> selectByBidderId(Connection connect, int bidderId) {
    String sql = "SELECT * FROM bid_transaction WHERE bidder_id=? ORDER BY bid_time DESC";
    List<BidTransaction> list = new ArrayList<>();
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, bidderId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(BidTransactionMapper.mapRow(rs));
        }
      }
      return list;

    } catch (Exception e) {
      throw new RuntimeException("Select bids by bidder failed", e);
    }
  }

  public BidTransaction findLatestByAuctionId(Connection connect, int auctionId) {
    String sql = "SELECT * FROM bid_transaction WHERE auction_id=? ORDER BY bid_time DESC LIMIT 1";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next())
          return null;
        return BidTransactionMapper.mapRow(rs);
      }

    } catch (Exception e) {
      throw new RuntimeException("Find latest bid failed", e);
    }
  }
}
