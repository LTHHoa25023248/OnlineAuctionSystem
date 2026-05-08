package com.example.auctionmanagementsystem.exception;

//Loi phien dau gia da ket thuc
public class AuctionClosedException extends AuctionException {
    public AuctionClosedException(){
        super("Action denied: This auction has already ended or is not active.");
    }
}

