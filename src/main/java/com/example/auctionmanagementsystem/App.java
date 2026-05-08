package com.example.auctionmanagementsystem; // Đúng package gốc của project

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Chạy màn hình đầu tiên là Login
        // Đường dẫn "View/auction_login.fxml" tính từ thư mục resources của package này
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("View/auction_login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Login | Auction System");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Hàm tiện ích dùng để chuyển đổi giữa các màn hình (Scene)
     * Bạn có thể gọi App.switchScene(...) từ bất kỳ Controller nào
     */
    public static void switchScene(Stage stage, String fxmlFileName, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("View/" + fxmlFileName));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle(title);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}