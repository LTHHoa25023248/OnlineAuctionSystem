package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.Auction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO implements DAOInterface<Auction> {

  @Override
  public int insert(Auction auction, Connection connect) {
    String sql = "INSERT INTO auction (item_id, seller_id, current_price, status, start_time, end_time, highest_bidder_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, auction.getItem().getId());
      ps.setInt(2, auction.getSeller().getId());
      ps.setDouble(3, auction.getCurrentPrice());
      ps.setString(4, auction.getStatus().name());
      ps.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
      ps.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
      // kiem tra xem highestBidder co null ko, boi luc khoi tao thi chua co ai dat gia
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
        throw new RuntimeException("Cannot get generated auction ID");
      }
    } catch (SQLException e) {
      throw new RuntimeException("Insert Auction failed", e);
    }
  }

  @Override
  public int update(Auction auction, Connection connect) {
    String sql = "UPDATE auction SET current_price=?, status=?, end_time=?, highest_bidder_id=?, reject_reason=? WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setDouble(1, auction.getCurrentPrice());
      ps.setString(2, auction.getStatus().name());
      ps.setTimestamp(3, Timestamp.valueOf(auction.getEndTime()));
      if (auction.getHighestBidder() != null) {
        ps.setInt(4, auction.getHighestBidder().getId());
      } else {
        ps.setNull(4, Types.INTEGER);
      }
     // luu ly do tu choi, neu khong co thi set Null nguoc lai 
      if (auction.getRejectReason() != null) {
        ps.setString(5, auction.getRejectReason());
      } else {
        ps.setNull(5, Types.VARCHAR);
      }
      ps.setInt(6, auction.getId()); 
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Update Auction failed for ID: " + auction.getId(), e);
    }
  }

  @Override
  public int delete(int id, Connection conn) {
    String sql = "DELETE FROM auction WHERE id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Delete Auction failed for ID: " + id, e);
    }
  }

  @Override
  public Auction selectById(int id, Connection connect) {
    String sql = "SELECT * FROM auction WHERE id=?";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        // goi ham mapRow de chuyen rs thanh oject Auction
        return AuctionMapper.mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("SelectById Auction failed for ID: " + id, e);
    }
  }

  @Override
  public List<Auction> selectAll(Connection connect) {
    List<Auction> list = new ArrayList<>();
    String sql = "SELECT * FROM auction ORDER BY id DESC";
    try (PreparedStatement ps = connect.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AuctionMapper.mapRow(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("SelectAll Auction failed", e);
    }
  }

 // danh sach dau gia cho admin duyet qua
  public List<Auction> selectPendingAuctions(Connection connect) {
    List<Auction> list = new ArrayList<>();
    //lay cac auctiion co statu=PENDING, sap xep theo id tang dan, ai tao truoc thi duoc xet truoc
    String sql = "SELECT * FROM auction WHERE status='PENDING' ORDER BY id ASC";
    try (PreparedStatement ps = connect.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AuctionMapper.mapRow(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("SelectPendingAuctions failed", e);
    }
  }

 
  public List<Auction> selectOpenAuctions(Connection connect) {
    List<Auction> list = new ArrayList<>();
    String sql = "SELECT * FROM auction WHERE status IN ('OPEN', 'RUNNING')";
    try (PreparedStatement ps = connect.prepareStatement(sql)) {
      // ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(AuctionMapper.mapRow(rs));
        }
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("SelectOpenAuctions failed", e);
    }
  }
}
