package com.example.auctionmanagementsystem.model;

public class Seller extends User {
    private String storeName;
    private double rating;

    public Seller(String username, String password, String email, String storeName) {
        super(username, password, email);
        this.storeName = storeName;
        this.rating = 0.0; // Điểm đánh giá ban đầu
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public void printInfo() {
        System.out.printf("[Seller] ID: %d | Username: %s | Store: %s | Rating: %.1f/5.0%n", 
                getId(), username, storeName, rating);
    }
}
