package com.example.auctionmanagementsystem.model;

public class Bidder extends User {
    private double balance;

    public Bidder(String username, String password, String email, double initialBalance) {
        super(username, password, email);
        this.balance = initialBalance;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // Phương thức hỗ trợ logic đấu giá
    public boolean deductBalance(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    // Thể hiện tính đa hình (Polymorphism) qua override
    @Override
    public void printInfo() {
        System.out.printf("[Bidder] ID: %d | Username: %s | Email: %s | Balance: $%.2f%n", 
                getId(), username, email, balance);
    }
}