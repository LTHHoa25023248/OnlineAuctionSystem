package com.example.auctionmanagementsystem.model;

import java.time.LocalDateTime;

public class Art extends Item {
    private String artist, theme, material;

    public Art(String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String artist, String theme, String material) {
        super(id, name, description, startingPrice, startTime, endTime);
        this.artist =  artist;
        this.theme = theme;
        this.material = material;
    }

    @Override
    public String getCategoryDetails() {
        return String.format("Artist: %s | Theme: %s | Material: %s", artist, theme, material);
    }
}
