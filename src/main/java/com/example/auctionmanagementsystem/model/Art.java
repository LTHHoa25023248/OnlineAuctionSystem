package com.example.auctionmanagementsystem.model;

public class Art extends Item {
    private String artist, theme, material;

    public Art(String name, String description, double startingPrice, String artist, String theme, String material) {
        super(name, description, startingPrice);
        this.artist =  artist;
        this.theme = theme;
        this.material = material;
    }

    public void setArtist(String newArtist) {this.artist = newArtist;}
    public void setTheme(String newTheme) {this.theme = newTheme;}
    public void setMaterial(String newMaterial) {this.material = newMaterial;}

    public String getArtist() {return artist;}
    public String getTheme() {return theme;}
    public String getMaterial() {return material;}

    @Override
    public String getCategoryDetails() {
        return String.format("Artist: %s | Theme: %s | Material: %s", artist, theme, material);
    }
}
