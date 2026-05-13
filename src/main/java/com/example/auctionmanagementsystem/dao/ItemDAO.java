package com.example.auctionmanagementsystem.dao;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.Art;
import com.example.auctionmanagementsystem.model.Electronics;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.model.Vehicle;

import java.sql.*;

import static java.lang.Integer.parseInt;

 public class ItemDAO implements DAOInterface <Item>{
    @Override
    public int insert(Item item){
        //Su dung commit, rollback. Commit de luu du lieu neu da insert thanh cong, neu khong thi rollback de tra ve trang thai ban dau luc chua insert
        //tranh truwowng hop bang item co du lieu nhung bang vehicle,... ko co su lieu

        Connection connect = null;
        try{
                 connect= DatabaseConnection.getConnection();
                 //luu du lieu tam thoi, ko luu vao database
                 connect.setAutoCommit(false);
            String sqlItem="INSERT INTO items(type, name, description, starting_price) VALUES(?,?,?,?)";

            //xac dinh loai item
            String type = "";
            if (item instanceof Vehicle){
                type="VEHICLE";
            } else if (item instanceof Art){
                type="ART";
            } else if (item instanceof Electronics){
                type="ELECTRONICS";
            }

            //Bang luu du lieu
            PreparedStatement psItem=connect.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS);
            psItem.setString(1,type);
            psItem.setString(2,item.getName());
            psItem.setString(3,item.getDescription());
            psItem.setDouble(4,item.getStartingPrice());
            psItem.executeUpdate();

            //lay ID tu dong sinh ra ru DB
            ResultSet  resultSet=psItem.getGeneratedKeys();
            int itemId = -1;
            if(resultSet.next()){
                itemId=resultSet.getInt(1);
                item.setId(Integer.valueOf(itemId));
            }
              //ko lay duoc id thi loi
            if(itemId == -1) {
                throw new RuntimeException("Cannot get generated item ID");
            }

            /**
             * Hàm được định nghĩa trong từng lớp cụ thể của Item (Lớp con Art, Electronics, Vehicle)
             */
            item.insertSubData(connect, itemId);

            //luu het du lieu neu tat ca ok
            connect.commit();
            return 1;//thanh cong
        } catch (Exception e){
            //rollback neu loi. Tro lai trang thai du lieu ban dau, huy toan bo thay doi
            try{
                if(connect != null){
                    connect.rollback();
                }
            } catch(SQLException sqle){
                sqle.printStackTrace();
            }
            e.printStackTrace();
        }finally {
            try{
                if (connect != null )
                    //tra ve trang thai tu dong luu du lieu xuong database
                    //ko thi executeUpdate ko tu dong luu xuong database nua
                    connect.setAutoCommit(true);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return 0;//that bai insert

    }

    @Override
    // return tra ve so co nghai la so dong thay doi thanh cong, tra ve 0 nghia la that bai
    // update khong duoc sua gia bat dau(starting_price)
    public int update(Item item){
        String sql="UPDATE items SET name=?, description=? WHERE id=?";
        try(Connection connect=DatabaseConnection.getConnection();
            PreparedStatement ps=connect.prepareStatement(sql)){
            ps.setString(1, item.getName());
            ps.setString(2,item.getDescription());
            ps.setInt(3, item.getId());
            return ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    //delete item
    public int delete(Item item){
        String sql="DELETE FROM items WHERE id=?";
        try(Connection connect=DatabaseConnection.getConnection();
          PreparedStatement ps=connect.prepareStatement(sql)){
            ps.setInt(1, item.getId());
            return ps.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    //tim bang id
    public Item selectById(String id){
        String sql="SELECT * FROM items WHERE id=?";
        try(Connection connect=DatabaseConnection.getConnection();
            PreparedStatement ps=connect.prepareStatement(sql)){
            ps.setInt(1, Integer.parseInt(id));
            ResultSet resultSet=ps.executeQuery();
            //tim lan luot, lay id va type, so sanh type xem vao truong hop nao
            if(resultSet.next()){
                int itemId=resultSet.getInt("id");
                String type=resultSet.getString("item_type");
                if("VEHICLE".equals(type)){
                    PreparedStatement ps1=connect.prepareStatement("SELECT * FROM vehicle_items WHERE item_id=?");
                    ps1.setInt(1,itemId);
                    ResultSet rs2=ps1.executeQuery();
                    if(rs2.next()){
                        return new Vehicle(resultSet.getString("name"),
                                   resultSet.getString("description"),
                                   resultSet.getDouble("starting_price"),
                                   rs2.getInt("year"),
                                   rs2.getDouble("mileage")
                        );
                    }
                }
                else if ("ART".equals(type)) {
                    PreparedStatement ps2 = connect.prepareStatement("SELECT * FROM art_items WHERE item_id=?");
                    ps2.setInt(1, itemId);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) {
                        return new Art(
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getDouble("starting_price"),
                                rs2.getString("artist"),
                                rs2.getString("theme"),
                                rs2.getString("material")
                        );
                    }
                }
                else if ("ELECTRONICS".equals(type)) {
                    PreparedStatement ps3 = connect.prepareStatement("SELECT * FROM electronics_items WHERE item_id=?");
                    ps3.setInt(1, itemId);
                    ResultSet rs3 = ps3.executeQuery();
                    if (rs3.next()) {
                        return new Electronics(
                                resultSet.getString("name"),
                                resultSet.getString("description"),
                                resultSet.getDouble("starting_price"),
                                rs3.getString("brand"),
                                rs3.getInt("warranty_months")
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
