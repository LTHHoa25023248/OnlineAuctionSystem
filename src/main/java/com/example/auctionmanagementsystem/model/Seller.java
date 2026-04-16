package com.example.auctionmanagementsystem.model;

public class Seller extends User {
    public Seller(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRoleName() {
        return "SELLER";
    }

    public void createItem() {
        System.out.println(username + " đang tạo sản phẩm đấu giá mới...");
    }
}
