package com.example.auctionmanagementsystem;

import com.example.auctionmanagementsystem.controller.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Auction Management System");
        primaryStage.setResizable(true);

        // Mở màn hình Login đầu tiên
        NavigationUtil.goTo(primaryStage, NavigationUtil.LOGIN);

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}