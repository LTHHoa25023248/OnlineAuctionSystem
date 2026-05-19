package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO implements DAOInterface<Auction>{
    public int insert(Auction auction){
        //gan voi null de rollback, va dong cac nguon tai nguyen
        Connection connect= null;
        PreparedStatement ps=null;
        ResultSet rs= null;
        try{
            connect=new DatabaseConnection().getConnection();
            connect.setAutoCommit(false);
            String sql ="INSERT INTO auction (item_id, seller_id, current_price, status, start_time, end_time, highest_bidder_id)"+
                    "VALUES(?,?,?,?,?,?,?)";
            ps=connect.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            //set du lieu vao sql
            ps.setInt(1,auction.getItem().getId());
            ps.setInt(2,auction.getSeller().getId());
            ps.setDouble(3,auction.getCurrentPrice());
            ps.setString(4,auction.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(6,Timestamp.valueOf(auction.getEndTime()));
            //luc moi tao auction, thuong chua co ai bid
            if (auction.getHighestBidder()!=null){
                ps.setInt(7,auction.getHighestBidder().getId());

            }else {
                //set NULL vao DB
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();
            //lay id tu sinh ra, roi gan lai
            rs=ps.getGeneratedKeys();
            int idAuction;
            if(rs.next()){
                idAuction=rs.getInt(1);
                auction.setId((idAuction));
            }else{
                throw new RuntimeException("Cannot get generated auction ID");
            }
            connect.commit();
            return idAuction;

        }catch(Exception e){
            try{
                if(connect!=null)
                    connect.rollback();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            throw new RuntimeException("Insert Auction failed",e);
        }finally{
            close(rs);
            close(ps);
            close(connect);
        }

    }


    public int delete(int id) {
        Connection connect = null;
        PreparedStatement ps = null;
        try {
            connect = new DatabaseConnection().getConnection();
            connect.setAutoCommit(false);
            String sql = "DELETE FROM auction WHERE id=?";
            ps = connect.prepareStatement(sql);
            ps.setInt(1, id);
            //return so dong bi xoa
            int result = ps.executeUpdate();
            connect.commit();
            return result;

        } catch (Exception e) {
            try {
                if (connect != null)
                    connect.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();

            }
            throw new RuntimeException("Delete failed", e);
        } finally {
            close(ps);
            close(connect);
        }
    }
        public int update(Auction auction){
            Connection connect=null;
            PreparedStatement ps=null;
            try{
                connect=new DatabaseConnection().getConnection();
                connect.setAutoCommit(false);
                String sql= "UPDATE auction SET current_price=?, status=?, end_time=? WHERE id=?";
                ps=connect.prepareStatement(sql);
                ps.setDouble(1,auction.getCurrentPrice());
                ps.setString(2,auction.getStatus().name());
                ps.setTimestamp(3,Timestamp.valueOf(auction.getEndTime()));
                ps.setInt(4, auction.getId());
                //Thuc thi update, tra ve so dong bi anh huong
                int affectedRows=ps.executeUpdate();
                connect.commit();
                return affectedRows;

            }catch (Exception e){
                //rollback neu loi, tra lai du lieu ban dau
                try{
                    if (connect!=null){
                        connect.rollback();
                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }
                throw new RuntimeException("Update auction failed",e);
            }finally{
                close(ps);
                close(connect);
            }


        }


    public Auction selectById(int id){
              Connection connect=null;
              PreparedStatement ps=null;
              ResultSet rs=null;
              try{
                  connect=new DatabaseConnection().getConnection();
                  String sql="SELECT * FROM auction WHERE id=?";
                  ps=connect.prepareStatement(sql);
                  //gan gia tri cho dau ?
                  ps.setInt(1,id);
                  rs=ps.executeQuery();
                  //khong co du lieu->tra ve null
                  if(!rs.next()){
                      return null;

                  }
                  return AuctionMapper.mapRow(rs);

              }catch(Exception e){
                  throw new RuntimeException("SelectById Auction failed", e);


              }finally{
                  close(rs);
                  close(ps);
                  close(connect);
              }
    }

              public List<Auction> selectAll() {
                  List<Auction> list = new ArrayList<>();
                  Connection connect = null;
                  PreparedStatement ps = null;
                  ResultSet rs = null;
                  ItemDAO itemDao = new ItemDAO();
                  try {
                      connect = new DatabaseConnection().getConnection();
                      //lay toan bo auction
                      String sql = "SELECT * FROM auction";
                      ps = connect.prepareStatement(sql);
                      rs = ps.executeQuery();
                      //dung while de lay du lieu tu DB
                      while (rs.next()) {
                          list.add(AuctionMapper.mapRow(rs));
                      }
                      return list;
                  } catch (Exception e) {
                      throw new RuntimeException("select all auction failed", e);
                  } finally {
                      close(rs);
                      close(ps);
                      close(connect);
                  }
              }
                  //Chon cac phien dau dang mo
                  public List<Auction> selectOpenAuctions() {
                      List<Auction> list = new ArrayList<>();
                      Connection connect = null;
                      PreparedStatement ps = null;
                      ResultSet rs = null;
                      try {
                          connect = new DatabaseConnection().getConnection();
                          String sql = "SELECT * FROM auction WHERE status ='OPEN' AND end_time>NOW()";
                          ps = connect.prepareStatement(sql);
                          rs = ps.executeQuery();
                          while (rs.next()) {
                              list.add(AuctionMapper.mapRow(rs));
                          }
                          return list;
                      } catch (Exception e) {
                          throw new RuntimeException("Select open auctions failed", e);
                      } finally {
                          close(rs);
                          close(ps);
                          close(connect);
                      }
                  }
              private void close(AutoCloseable c){
                     if(c!= null){
                       try{
                         c.close();
                      }catch(Exception e){
                          e.printStackTrace();
            }
        }
    }
}







