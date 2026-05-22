package com.example.auctionmanagementsystem.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Art extends Item {
  private String artist, theme, material;

  public Art(String name, String description, double startingPrice, String artist, String theme,
      String material) {
    super(name, description, startingPrice);
    this.artist = artist;
    this.theme = theme;
    this.material = material;
  }

  public void setArtist(String newArtist) {
    this.artist = newArtist;
  }

  public void setTheme(String newTheme) {
    this.theme = newTheme;
  }

  public void setMaterial(String newMaterial) {
    this.material = newMaterial;
  }

  public String getArtist() {
    return artist;
  }

  public String getTheme() {
    return theme;
  }

  public String getMaterial() {
    return material;
  }

  @Override
  public String getCategoryDetails() {
    return String.format("Artist: %s | Theme: %s | Material: %s", artist, theme, material);
  }

  @Override
  public void insertSubData(Connection conn, int itemId) throws SQLException {
    String sql = "INSERT INTO art_items(item_id, artist, theme, material) VALUES (?,?,?,?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ps.setString(2, this.artist);
      ps.setString(3, this.theme);
      ps.setString(4, this.theme);
      ps.executeUpdate();
    }
  }

  @Override
  public void updateSubData(Connection conn) throws SQLException {
    String sql = "UPDATE items SET artist=?, theme =?, material =? WHERE id=?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, this.artist);
      ps.setString(2, this.theme);
      ps.setString(3, this.material);
      ps.executeUpdate();
    }
  }
}

