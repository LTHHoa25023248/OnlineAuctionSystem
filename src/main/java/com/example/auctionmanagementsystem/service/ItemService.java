package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.ItemDAO;
import com.example.auctionmanagementsystem.model.Item;

import java.sql.Connection;

public class ItemService {
    private final ItemDAO itemDAO = new ItemDAO();

    public int creatItem(Item item) {
        Connection connect = null;
        try {
            connect = new DatabaseConnection().getConnection();
            //luu du lieu tam thoi, chua ghi xuong vao DB
            connect.setAutoCommit(false);
            // insert du lieu bang item
            int itemId = itemDAO.insert(item,connect);
            connect.commit();
            return itemId;

        } catch (Exception e) {
            try {
                if (connect != null) {
                    //loi du lieu thi thi tra ve trag thai ban dau
                    connect.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
            throw new RuntimeException("Create item failed", e);

        } finally {
            //dong connect
            try {
                if (connect != null) {
                    connect.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateItem(Item item) {
        Connection connect = null;
        try {
            connect = new DatabaseConnection().getConnection();
            connect.setAutoCommit(false);
            itemDAO.update( item,connect);
            connect.commit();

        } catch (Exception e) {
            try {
                if (connect != null) {
                    connect.rollback();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException(
                    "Update item failed",
                    e
            );

        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void deleteItem(Item item) {
        Connection connect = null;
        try {
            connect = new DatabaseConnection().getConnection();
            connect.setAutoCommit(false);
           itemDAO.delete(item,connect);
            connect.commit();

        } catch (Exception e) {
            try {
                if (connect != null) {
                    connect.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Delete item failed", e);

        } finally {
            try {
                if (connect != null) {
                    connect.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public Item getItemById(int id) {
        try (Connection connect = new DatabaseConnection().getConnection()) {
            return itemDAO.selectById( id,connect);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Get item failed",
                    e
            );
        }
    }
}



