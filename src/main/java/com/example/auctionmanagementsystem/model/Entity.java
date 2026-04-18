package com.example.auctionmanagementsystem.model;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity {
    private static final AtomicInteger AUTO_ID = new AtomicInteger(1);
    protected int id;

    public Entity() {
        this.id = AUTO_ID.getAndIncrement();
    }

    public int getId() {
        return id;
    }
}
