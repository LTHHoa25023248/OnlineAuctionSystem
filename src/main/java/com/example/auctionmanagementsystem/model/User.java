package com.example.auctionmanagementsystem.model;

public abstract class User extends Entity {
    protected String username;
    protected String password;
    public User(String id, String username, String password){
        super(id);
        this.username=username;
        this.password=password;

    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
}
