package com.example.auctionmanagementsystem.model;

public abstract class Entity {
    protected int id;
//Constructor mặc định, id=0 được hiểu là chưa được lưu vào Database
    public Entity() {
        this.id = 0;
    }

    public int getId() {
        return id;
    }

    // Thêm setter để DAO có thể cập nhật ID sau khi Database sinh ra
    public void setId(int id) {
        this.id = id;
    }
}
