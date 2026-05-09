package com.example.auctionmanagementsystem.exception;

//Loi dat gia
public class InvalidBidException extends AuctionException{
    public InvalidBidException(double attemptedAmount, double currentPrice){
        super("Bid rejected: Your amount ("+attemptedAmount+") must be higher than the current price (" +currentPrice+").");
    }
}

