package com.example.auctionmanagementsystem.model;

import java.util.Map;

public class ItemFactory {
  public static Item createItem(String type, String name, String description, double startingPrice, Map<String, String> attributes ) {
        //Dùng 'type' để xác định danh mục sản phẩm
        //Không được để trống mục 'type' khi tạo sản phẩm
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("You must enter product category!");
        }

        //Tránh NullPointerException cho attributes khi tạo sản phẩm
        if (attributes == null) {
            throw new IllegalArgumentException("Please add more information!");
        }

        //Khởi tạo sản phẩm cho từng loại 
        try {
            return switch(type.toUpperCase()) {
            case "ELECTRONICS" -> {
                String brand = attributes.getOrDefault("brand", "Unknown");
                String warrantyMonthsStr = attributes.getOrDefault("warranty", "0");
                yield new Electronics(name, description, startingPrice, brand, Integer.parseInt(warrantyMonthsStr.trim()));
            }

            case "ART" -> {
                String artist = attributes.getOrDefault("artist", "Unknown");
                String material = attributes.getOrDefault("material", "Unknown");
                String theme = attributes.getOrDefault("theme", "Unknown");
                yield new Art(name, description, startingPrice, artist, theme, material);
            }

            case "VEHICLE" -> {
                String yearStr = attributes.getOrDefault("year", "0");
                String mileageStr = attributes.getOrDefault("mileage", "0.0");
                yield new Vehicle(name, description, startingPrice, Integer.parseInt(yearStr.trim()), Double.parseDouble(mileageStr.trim()));
            }

            //Thông báo không tìm thấy loại sản phẩm phù hợp khi tạo sản phẩm (chưa có trong danh sách loại sản phẩm)
            default -> throw new IllegalArgumentException("Your product has not supported by our system yet!");
            };
        } catch (Exception e) {
            System.err.println("ERROR: Found error in ItemFactory.java!" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create item", e);
        }

    }
}
