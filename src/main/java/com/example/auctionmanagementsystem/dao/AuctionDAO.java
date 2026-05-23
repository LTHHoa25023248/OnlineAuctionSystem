package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.model.Auction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO implements DAOInterface<Auction> {

  @Override
  public int insert(Auction auction, Connection conn) {
    String sql = "INSERT INTO auction (item_id, seller_id, current_price, status, start_time, end_time, highest_bidder_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

    // Sử dụng try-with-resources để tự động đóng PreparedStatement
    try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setInt(1, auction.getItem().getId());
      ps.setInt(2, auction.getSeller().getId());
      ps.setDouble(3, auction.getCurrentPrice());
      ps.setString(4, auction.getStatus().name());
      ps.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
      ps.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));

      if (auction.getHighestBidder() != null) {
        ps.setInt(7, auction.getHighestBidder().getId());
      } else {
        ps.setNull(7, Types.INTEGER);
      }

      ps.executeUpdate();

      // Sử dụng try-with-resources lồng nhau cho ResultSet
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          int id = rs.getInt(1);
          auction.setId(id);
          return id;
        }
        throw new RuntimeException("Cannot get generated auction ID");
      }
    } catch (SQLException e) {
      // Bọc Checked Exception thành Unchecked Exception
      throw new RuntimeException("Insert Auction failed", e);
    }
  }

  @Override
  public int update(Auction auction, Connection conn) {
    // ===== SỬA: thêm cột reject_reason vào câu UPDATE =====
    String sql = "UPDATE auction SET current_price=?, status=?, end_time=?, highest_bidder_id=?, reject_reason=? WHERE id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, auction.getCurrentPrice());
      ps.setString(2, auction.getStatus().name());
      ps.setTimestamp(3, Timestamp.valueOf(auction.getEndTime()));

      if (auction.getHighestBidder() != null) {
        ps.setInt(4, auction.getHighestBidder().getId());
      } else {
        ps.setNull(4, Types.INTEGER);
      }

      // ===== THÊM MỚI: lưu lý do từ chối nếu có, ngược lại set NULL =====
      if (auction.getRejectReason() != null) {
        ps.setString(5, auction.getRejectReason());
      } else {
        ps.setNull(5, Types.VARCHAR);
      }
      // ===== END THÊM MỚI =====

      ps.setInt(6, auction.getId()); // index tăng từ 5 lên 6 do thêm tham số
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
  public Auction selectById(int id, Connection conn) {
    String sql = "SELECT * FROM auction WHERE id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return AuctionMapper.mapRow(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException("SelectById Auction failed for ID: " + id, e);
    }
  }

  @Override
  public List<Auction> selectAll(Connection conn) {
    List<Auction> list = new ArrayList<>();
    String sql = "SELECT * FROM auction ORDER BY id DESC";
    // Khai báo nhiều tài nguyên trong cùng 1 khối try
    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AuctionMapper.mapRow(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("SelectAll Auction failed", e);
    }
  }

  // ===== THÊM MỚI: lấy danh sách phiên đấu giá đang chờ admin duyệt =====
  public List<Auction> selectPendingAuctions(Connection conn) {
    List<Auction> list = new ArrayList<>();
    // Chỉ lấy các auction có status = PENDING, sắp xếp theo id tăng dần (ai tạo trước xét trước)
    String sql = "SELECT * FROM auction WHERE status='PENDING' ORDER BY id ASC";
    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        list.add(AuctionMapper.mapRow(rs));
      }
      return list;
    } catch (SQLException e) {
      throw new RuntimeException("SelectPendingAuctions failed", e);
    }
  }
  // ===== END THÊM MỚI =====

  // Không cần Override nếu Interface không yêu cầu hàm đặc thù này
  public List<Auction> selectOpenAuctions(Connection conn) {
    List<Auction> list = new ArrayList<>();
    // Tránh dùng NOW() của database, ưu tiên dùng tham số để kiểm soát Timezone
    String sql = "SELECT * FROM auction WHERE status='RUNNING' AND end_time > ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      // Truyền thời gian hiện tại từ tầng ứng dụng Java
      ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
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
