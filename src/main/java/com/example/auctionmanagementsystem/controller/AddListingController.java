package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.App;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class AddListingController {

    // Hàm này sẽ chạy khi bạn click chuột vào nút X (ImageView)
    @FXML
    protected void onCloseButtonClick(MouseEvent event) {
        try {
            // 1. Lấy ra cửa sổ hiện tại
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 2. Chuyển về màn hình danh sách (auction_list.fxml)
            App.switchScene(stage, "auction_list.fxml", "Auction List");

        } catch (IOException e) {
            System.out.println("Lỗi khi quay lại màn hình chính: " + e.getMessage());
            e.printStackTrace();
        }
    }
}