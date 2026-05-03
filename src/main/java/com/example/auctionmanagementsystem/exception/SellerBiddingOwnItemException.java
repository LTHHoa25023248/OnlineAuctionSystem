package com.example.auctionmanagementsystem.exception;

//Nguoi ban ko dat gia cho san pham cua minh ban
public class SellerBiddingOwnItemException extends AuctionException{
    public SellerBiddingOwnItemException() {
        super("Sellers cannot bid on their own items.");
    }

}
