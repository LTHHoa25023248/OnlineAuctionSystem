package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

public class AuctionListController {

    // 1. Hàm xử lý nút Log Out
    @FXML
    protected void onLogOutButtonClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            App.switchScene(stage, "auction_login.fxml", "Login | Auction System");
        } catch (IOException e) {
            System.out.println("Lỗi khi chuyển cảnh đăng xuất: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 2. Hàm xử lý nút Sell
    @FXML
    protected void onSellButtonClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // File này nằm trong thư mục components
            App.switchScene(stage, "components/add_listing.fxml", "Add New Listing | Auction System");
        } catch (IOException e) {
            System.out.println("Lỗi khi mở trang đăng bài: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 3. Hàm xử lý nút Sort By
    @FXML
    protected void onSortByButtonClick(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // LƯU Ý: File này cũng nằm trong components/ nên phải thêm đường dẫn vào
            App.switchScene(stage, "components/sortingmenu.fxml", "Sort Menu");
        } catch (IOException e) {
            System.out.println("Lỗi khi mở menu sắp xếp: " + e.getMessage());
            e.printStackTrace();
        }
    }

} // Dấu đóng class phải nằm ở cuối cùng như thế này

