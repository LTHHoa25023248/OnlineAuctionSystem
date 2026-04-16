package com.example.auctionmanagementsystem.model;

public class Bidder extends User {
    public Bidder(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRoleName() {
        return "BIDDER";
    }

    public void placeBid() {
        System.out.println(username + " đang thực hiện đặt giá...");
    }
}
