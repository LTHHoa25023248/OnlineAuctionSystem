package com.example.auctionmanagementsystem;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.server.AuctionServer;
import com.example.auctionmanagementsystem.service.AuctionScheduler;



public class Main {

  public static void main(String[] args) {
    boolean isServerMode = false;
    if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
      isServerMode = true;
    }

    if (isServerMode) {
      if (!DatabaseConnection.testConnection()) {
        System.err.println("[Main-Server] Cannot connect to database.");
        System.err.println("[Main-Server] Check database.properties or DatabaseConnection.java");
        System.exit(1);
      }
      System.out.println("[Main-Server] Database connected.");

      try {
        AuctionServer.start();
        System.out.println("[Main-Server] HTTP Server started successfully.");
      } catch (Exception e) {
        System.err.println("[Main-Server] Failed to start HTTP server: " + e.getMessage());
        System.exit(1);
      }
      AuctionScheduler.getInstance().start();
      System.out.println("[Main-Server] AuctionScheduler cron-job is running...");
      System.out.println("[Main-Server] Server is fully operational. Press Ctrl+C to stop.");
    }
    else {
      System.out.println("[Main-Client] Launching JavaFX Graphical User Interface...");
      App.main(args);
    }
  }
}

