package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;

import java.io.Closeable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;


public class ItemDAO implements DAOInterface <Item> {
     public int insert(Item item) {
         //Su dung commit, rollback. Commit de luu du lieu neu da insert thanh cong, neu khong thi rollback de tra ve trang thai ban dau luc chua insert
         //tranh truong hop bang item co du lieu nhung bang vehicle,... ko co du lieu
         //gan null de dong cac nguon tai nguyen, rollback tranh mat du lieu
         Connection connect = null;
         PreparedStatement ps=null;
         ResultSet rs =null;
         try {
             connect = new DatabaseConnection().getConnection();
             //luu du lieu tam thoi, ko luu vao database
             //tat auto commit, dam bao insert cha+con thanh cong hoac that bai cung luc
             connect.setAutoCommit(false);
             String sqlItem = "INSERT INTO items( name, description, starting_price, item_type) VALUES(?,?,?,?)";

             //Bang luu du lieu
             ps = connect.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS);
             ps.setString(1, item.getName());
             ps.setString(2, item.getDescription());
             ps.setDouble(3, item.getStartingPrice());
             ps.setString(4, item.getClass().getSimpleName().toUpperCase());
             //Thuc thi insert
             ps.executeUpdate();
             //lay id tu tang tu database
             rs = ps.getGeneratedKeys();
             int itemId;
             if (rs.next()) {
                 itemId = rs.getInt(1);
                 item.setId(itemId);
             } else {
                 throw new RuntimeException("Cannot get generated ID");
             }
             //Insert bang con
             //item tu biet no thuoc loai nao va tu insert
             item.insertSubData(connect, itemId);
             // commit du lieu, luu vao data
             connect.commit();
             return itemId;
         } catch (Exception e) {
             //nêú có lỗi xảy ra-> rolllback toan bo, neu luu du lieu sai thi tro ve trang thai ban dau
             try {
                 if (connect != null)
                     connect.rollback();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             throw new RuntimeException("Insert item failed",e);
         } finally {
             //dong connection
             close( ps);
             close( connect);

         }

     }

     @Override
     public int update(final Item item) {
         Connection connect=null;
         PreparedStatement ps= null;
         try {
             connect = new DatabaseConnection().getConnection();
             connect.setAutoCommit(false);
             //update bang cha
             String sql = "UPDATE items SET name=?, description =? WHERE id=?";
             ps = connect.prepareStatement(sql);
             ps.setString(1, item.getName());
             ps.setString(2, item.getDescription());
             ps.setInt(3, item.getId());
             //so dong bi anh huong
              int affectedRows =ps.executeUpdate();
             //update bang con
             item.updateSubData(connect);
             connect.commit();
             return affectedRows;
         } catch (Exception e) {
             try{
                 if (connect!= null)
                     connect.rollback();

         }catch(Exception ex){
                ex.printStackTrace();
             }
            throw new RuntimeException("Update failed",e);
         }finally{
            close( ps);
            close( connect);
         }
     }


     public int delete(int id) {
         Connection connect=null;
         PreparedStatement ps=null;
         try  {
             connect =new DatabaseConnection().getConnection();
             connect.setAutoCommit(false);
             //chi can xoa bang cha (CASCADE lo phan con)
             String sql = "DELETE FROM items WHERE id =?";
             ps = connect.prepareStatement(sql);
             ps.setInt(1, id);
             //return lai so dong bi xoa
             int result=ps.executeUpdate();
             connect.commit();
             return result;
         } catch (Exception e) {
             try{
                 if(connect!=null)
                     connect.rollback();
             }catch(Exception ex){
                 ex.printStackTrace();
             }
             throw new RuntimeException("Delete failed", e);
         } finally {
             close(ps);
             close(connect);
         }
     }


     public Item selectById(int id) {
         try(Connection connect =new DatabaseConnection().getConnection()){
             String sql ="SELECT * FROM items WHERE id =?";
             PreparedStatement ps= connect.prepareStatement(sql);
             ps.setInt(1,id);
             ResultSet rs=ps.executeQuery();
             if (!rs.next())
                 return null;
             String type=rs.getString("item_type");
             String name =rs.getString("name");
             String description=rs.getString("description");
             double starting_price=rs.getDouble("starting_price");
            //Dung Map de chua thuoc tinh dac thu cua tung loai
             Map<String,String> attributes =new HashMap<>();
             //Truy van bang con dua tren loai
             String subTableSql=switch(type){
                 case "VEHICLE"-> "SELECT * FROM vehicle_items WHERE item_id=?";
                 case "ART"->"SELECT * FROM art_items WHERE item_id=?";
                 case "ELECTRONICS"->"SELECT * FROM electronics_items where item_id=?";
                 default->null;
             };
             if (subTableSql != null) {
                 try (PreparedStatement psSub = connect.prepareStatement(subTableSql)) {
                     psSub.setInt(1, id);
                     try (ResultSet rsSub = psSub.executeQuery()) {
                         if (rsSub.next()) {
                             // tuy theo loai ma boc du lieu vao map
                             if ("VEHICLE".equals(type)) {
                                 attributes.put("year", String.valueOf(rsSub.getInt("year")));
                                 attributes.put("mileage", String.valueOf(rsSub.getDouble("mileage")));
                             } else if ("ART".equals(type)) {
                                 attributes.put("artist", rsSub.getString("artist"));
                                 attributes.put("theme", rsSub.getString("theme"));
                                 attributes.put("material", rsSub.getString("material"));
                             } else if ("ELECTRONICS".equals(type)) {
                                 attributes.put("brand", rsSub.getString("brand"));
                                 attributes.put("warranty", String.valueOf(rsSub.getInt("warranty_months")));
                             }
                         }
                     }
                 }
             }
             //goi factoryItem de tao doi tuong
             Item item = ItemFactory.createItem(type, name, description, starting_price, attributes);
             item.setId(id);
             return item;
         }catch(Exception e) {
             throw new RuntimeException("Select failed", e);
         }
     }
     // phuong thuc close
    public void close(AutoCloseable c){
         if(c!= null){
             try{
                 c.close();
             }catch(Exception e){
                 e.printStackTrace();
             }
         }
    }
 }


