package com.example.auctionmanagementsystem.exception;

public class InsufficientBalanceException extends AuctionException {
    public InsufficientBalanceException(double required, double available) {
        super(String.format(
                "Insufficient balance: required %,.2f USD but only %,.2f USD available.",
                required, available));
    }
}
