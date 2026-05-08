package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AuctionDetailController {

    // 1. Khai báo các thành phần muốn tương tác (nhớ khớp fx:id)
    @FXML
    private TextField bidField; // Ô nhập tiền

    @FXML
    private Button placeBidButton; // Nút đặt cược

    @FXML
    private Label bidValidationLabel; // Dòng chữ báo lỗi đỏ đỏ

    // 2. Hàm khởi tạo (chạy ngay khi bật màn hình)
    @FXML
    public void initialize() {
        // Tạm thời ẩn dòng chữ báo lỗi đi
        bidValidationLabel.setVisible(false);
    }

    // 3. ĐÂY LÀ HÀM SẼ CHẠY KHI BẤM NÚT ĐẶT CƯỢC
    @FXML
    public void handlePlaceBid() {
        // Lấy chữ người dùng nhập vào
        String tienCuoc = bidField.getText();

        if (tienCuoc.isEmpty()) {
            // Nếu không nhập gì mà bấm nút -> Hiện chữ cảnh báo
            bidValidationLabel.setText("Vui lòng nhập số tiền!");
            bidValidationLabel.setVisible(true);
        } else {
            // Nếu có nhập -> In ra thử
            System.out.println("Bạn vừa đặt cược số tiền là: " + tienCuoc + " $");
            bidValidationLabel.setVisible(false);

            // Xóa trắng ô nhập liệu sau khi bấm
            bidField.clear();
        }
    }

    // =========================================================
    // 4. ĐÂY LÀ HÀM MỚI THÊM VÀO: CHUYỂN TRANG KHI BẤM NÚT X
    // =========================================================
    @FXML
    public void handleCloseButton(javafx.scene.input.MouseEvent event) {
        try {
            // Bước 1: Gọi file giao diện màn hình Danh sách (auction_list.fxml) ra
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/auctionmanagementsystem/View/auction_list.fxml"));
            javafx.scene.Parent root = loader.load();

            // Bước 2: Bắt lấy cái "Cửa sổ" (Stage) hiện tại đang chứa cái nút X mà bạn vừa bấm
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Bước 3: Đắp cái giao diện Danh sách mới lên cái Cửa sổ đó
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (java.io.IOException e) {
            e.printStackTrace(); // Báo lỗi đỏ ở console nếu gõ sai tên file FXML
            System.out.println("Lỗi: Không tìm thấy file auction_list.fxml");
        }
    }}