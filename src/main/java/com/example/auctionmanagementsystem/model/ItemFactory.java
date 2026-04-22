package com.example.auctionmanagementsystem.model;
import java.time.LocalDateTime;

public class ItemFactory {
    public static Item createItem(String type, String id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String... extra) {
        //Using 'type' to determine the category of Item 
        //Must not be null
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("You must enter product category!");
        }

        //Initialize item based on type 
        try {
            return switch(type.toUpperCase()) {
            case "ELECTRONICS" -> {
                if (extra.length < 2) throw new IllegalArgumentException("Lack of Electronics' parameters!");
                yield new Electronics(id, name, description, startingPrice, extra[0], Integer.parseInt(extra[1]));
            }

            case "ART" -> {
                if (extra.length < 3) throw new IllegalArgumentException("Lack of Art's parameters!");
                yield new Art(id, name, description, startingPrice, extra[0], extra[1], extra[2]);
            }

            case "VEHICLE" -> {
                if (extra.length < 2) throw new IllegalArgumentException("Lack of Vehicle's parameters!");
                yield new Vehicle(id, name, description, startingPrice, Integer.parseInt(extra[0]), Double.parseDouble(extra[1]));
            }

            default -> throw new IllegalArgumentException("Product is not supported by our system!");
            };
        } catch (Exception e) {
            System.err.println("ERROR: Found error in ItemFactory.java!" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create item", e);
        }

    }
}
