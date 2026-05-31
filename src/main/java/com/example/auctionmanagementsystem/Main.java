package com.example.auctionmanagementsystem;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.server.AuctionServer;
import com.example.auctionmanagementsystem.service.AuctionScheduler;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {

    if (!DatabaseConnection.testConnection()) {
      System.err.println("[Main] Cannot connect to DB.");
      System.exit(1);
    }

    // Kiểm tra xem server đã chạy chưa (port 8080 đã bị chiếm chưa)
    if (isServerRunning()) {
      // Server đã chạy ở cửa sổ khác → chỉ mở thêm giao diện JavaFX
      System.out.println("[Main] Server already running — launching additional client window.");
    } else {
      // Server chưa chạy → khởi động server + scheduler
      try {
        AuctionServer.start();
        System.out.println("[Main] Server started on port 8080.");
      } catch (Exception e) {
        System.err.println("[Main] Failed to start server: " + e.getMessage());
        System.exit(1);
      }
      AuctionScheduler.getInstance().start();
      System.out.println("[Main] Scheduler started.");
    }

    App.main(args);
  }

  //Trả về true nếu port 8080 đang có gì đó lắng nghe (server đã chạy). */
  private static boolean isServerRunning() {
    try (Socket s = new Socket("localhost", 8080)) {
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}


