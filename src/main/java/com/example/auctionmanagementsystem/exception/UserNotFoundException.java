package com.example.auctionmanagementsystem.exception;

//Loi tai khoan khong ton tai
public class UserNotFoundException extends AuctionException{
    public UserNotFoundException(){
        super("Access denied: User could not be found in the system.");

    }
}

