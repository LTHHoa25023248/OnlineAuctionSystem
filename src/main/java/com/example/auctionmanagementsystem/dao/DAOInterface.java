package com.example.auctionmanagementsystem.dao;



import java.sql.Connection;

import java.util.List;



public interface DAOInterface<T> {



// Thêm dữ liệu (trả về ID vừa tạo hoặc số dòng ảnh hưởng)

  int insert(T obj, Connection conn);



// Cập nhật dữ liệu

  int update(T obj, Connection conn);



// Xóa dữ liệu theo ID

  int delete(int id, Connection conn);



// Tìm một bản ghi theo ID

  T selectById(int id, Connection conn);



// Lấy tất cả bản ghi

  List<T> selectAll(Connection conn);

}
