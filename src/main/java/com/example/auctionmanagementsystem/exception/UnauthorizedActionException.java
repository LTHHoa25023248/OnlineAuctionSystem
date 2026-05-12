package com.example.auctionmanagementsystem.exception;

//Nguoi dung ko co quyen lam mot viecj gi do
public class UnauthorizedActionException extends AuctionException{
    public UnauthorizedActionException() {
        super("You are not allowed to perform this action.");
    }

}
