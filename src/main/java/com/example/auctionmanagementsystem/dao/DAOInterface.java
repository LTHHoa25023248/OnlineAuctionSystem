package com.example.auctionmanagementsystem.dao;

import java.util.ArrayList;

public interface DAOInterface<T> {
  // them du lieu
  int insert(T obj);


  // tim theo id
  T selectById(int id);

  // Cap nhat du lieu
  int update(T obj);

  // Xoa du lieu
  int delete(int id);



}
