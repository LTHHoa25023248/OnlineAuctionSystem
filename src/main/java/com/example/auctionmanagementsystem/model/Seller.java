package com.example.auctionmanagementsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private List<Item> listedItems;
    public Seller(String username, String password) {
        super(username, password);
        this.listedItems = new ArrayList<>();
    }

    public List<Item> getListedItems() {
        return listedItems;
    }

    @Override
    public String getRoleName() {
        return "Seller: " + username;
    }

    //Specific method: Uploading a new item
    public void addListing(Item newItem) {
        if (newItem != null) {
            this.listedItems.add(newItem);
        }
    }

}
