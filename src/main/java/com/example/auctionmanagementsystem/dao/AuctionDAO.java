package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.Bidder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

  public int insert(Auction auction, Connection connect) throws SQLException {
    String sql =
        "INSERT INTO auction (item_id, seller_id, current_price, status, start_time, end_time, highest_bidder_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    // sau khi chen xong giu lai id tu dong sinh ra
    try (PreparedStatement ps = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, auction.getItem().getId());
      ps.setInt(2, auction.getSeller().getId());
      ps.setDouble(3, auction.getCurrentPrice());
      ps.setString(4, auction.getStatus().name());
      ps.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
      ps.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
      // kiem tra highestBidder. Luc dau thi chua co ai bid, gan voi null
      if (auction.getHighestBidder() != null) {
        ps.setInt(7, auction.getHighestBidder().getId());
      } else {
        ps.setNull(7, Types.INTEGER);
      }
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          int id = rs.getInt(1);
          auction.setId(id);
          return id;
        }
      }
      throw new RuntimeException("Cannot get generated auction ID");
    }
  }


  public int update(Auction auction, Connection connect) throws SQLException {
    String sql =
        "UPDATE auction SET current_price=?, status=?, end_time=?, highest_bidder_id=? WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setDouble(1, auction.getCurrentPrice());
      ps.setString(2, auction.getStatus().name());
      ps.setTimestamp(3, Timestamp.valueOf(auction.getEndTime()));
      if (auction.getHighestBidder() != null) {
        ps.setInt(4, auction.getHighestBidder().getId());
      } else {
        ps.setNull(4, Types.INTEGER);
      }
      ps.setInt(5, auction.getId());
      return ps.executeUpdate();
    }
  }

  public int delete(int id, Connection connect) throws SQLException {
    String sql = "DELETE FROM auction WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate();
    }
  }

  public Auction selectById(int id, Connection connect) throws SQLException {
    String sql = "SELECT * FROM auction WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next())
          return null;
        return AuctionMapper.mapRow(rs);
      }
    }
  }

  public List<Auction> selectAll(Connection connect) throws SQLException {
    List<Auction> list = new ArrayList<>();
    String sql = "SELECT * FROM auction ORDER BY id DESC";
    try (PreparedStatement ps = connect.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AuctionMapper.mapRow(rs));
      }
    }

    return list;
  }

  public List<Auction> selectOpenAuctions(Connection connect) throws SQLException {
    List<Auction> list = new ArrayList<>();
    String sql = "SELECT * FROM auction WHERE status='RUNNING' AND end_time > NOW()";
    try (PreparedStatement ps = connect.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AuctionMapper.mapRow(rs));
      }
    }
    return list;
  }
}
