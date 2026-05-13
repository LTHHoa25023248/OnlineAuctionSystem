package com.example.auctionmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * DepositController — Điều khiển màn hình nạp tiền (deposit.fxml).
 *
 * Popup mở từ ProfileController (depositButton).
 *
 * Tính năng:
 *   - Hiển thị số dư hiện tại
 *   - Chọn nhanh số tiền qua 8 preset buttons (10, 50, 100, 500, 1000, 2000, 5000, 10000)
 *   - Nhập số tiền thủ công
 *   - Validate và cộng vào số dư
 *
 * Lưu ý: Button text trong FXML là số thuần (10, 50, 100...) không có "$" hay ","
 * để tránh lỗi FXML parser (dấu $ được parser hiểu là binding expression).
 *
 * TODO: Kết nối DB để lưu giao dịch và cập nhật balance thật.
 */
public class DepositController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private Label        balanceLabel;   // hiển thị số dư hiện tại
    @FXML private MFXTextField amountField;    // input số tiền thủ công
    @FXML private Label        validationLabel;// thông báo lỗi hoặc thành công
    @FXML private MFXButton    depositButton;  // nút "Deposit Now"
    @FXML private MFXButton    closeButton;    // nút đóng popup

    // 8 preset buttons — text là số thuần không có ký tự đặc biệt
    @FXML private MFXButton btn10;
    @FXML private MFXButton btn50;
    @FXML private MFXButton btn100;
    @FXML private MFXButton btn500;
    @FXML private MFXButton btn1000;
    @FXML private MFXButton btn2000;
    @FXML private MFXButton btn5000;
    @FXML private MFXButton btn10000;

    // Số dư tạm thời trong session (chưa lưu DB)
    private double currentBalance = 500.00;

    @FXML
    public void initialize() {
        validationLabel.setText("");
        updateBalanceLabel(); // hiển thị số dư ban đầu
    }

    /**
     * Xử lý click preset button (10, 50, 100...).
     * Lấy text của button (số thuần) và điền vào amountField.
     * Dùng ActionEvent thay MouseEvent vì FXML dùng onAction.
     *
     * @param e ActionEvent chứa source là MFXButton được click
     */
    @FXML
    private void onQuickAmount(ActionEvent e) {
        MFXButton clicked = (MFXButton) e.getSource();
        // Button text là số thuần "1000", set thẳng vào field
        amountField.setText(clicked.getText());
        validationLabel.setText(""); // xóa thông báo cũ
    }

    /**
     * Xử lý nạp tiền khi bấm "Deposit Now".
     * Validate amount rồi cộng vào currentBalance.
     *
     * Quy tắc validate:
     *   - Không được để trống
     *   - Phải là số dương
     *   - Tối thiểu 10 USD
     *   - Tối đa 100,000 USD
     */
    @FXML
    private void handleDeposit() {
        // Reset thông báo
        validationLabel.setStyle("-fx-text-fill: #E05454;");
        validationLabel.setText("");

        // Xóa dấu phẩy nếu user tự nhập "1,000"
        String raw = amountField.getText().trim().replace(",", "");

        if (raw.isEmpty()) {
            validationLabel.setText("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(raw);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            validationLabel.setText("Invalid amount.");
            return;
        }

        if (amount < 10) {
            validationLabel.setText("Minimum deposit is 10.00 USD");
            return;
        }
        if (amount > 100000) {
            validationLabel.setText("Maximum deposit is 100,000.00 USD");
            return;
        }

        // Nạp thành công
        currentBalance += amount;
        updateBalanceLabel();
        amountField.setText("");

        // Hiển thị thông báo thành công màu xanh
        validationLabel.setStyle("-fx-text-fill: #3DBA7F;");
        validationLabel.setText("Deposit successful! +" + formatMoney(amount) + " USD");

        // TODO: TransactionDAO.deposit(SessionManager.getInstance().getUserId(), amount);
    }

    /** Cập nhật label số dư với format có dấu phẩy (vd: 1,500.00 USD) */
    private void updateBalanceLabel() {
        balanceLabel.setText(formatMoney(currentBalance) + " USD");
    }

    /**
     * Format số tiền với dấu phẩy ngăn cách hàng nghìn và 2 chữ số thập phân.
     * Ví dụ: 1500.5 → "1,500.50"
     */
    private String formatMoney(double amount) {
        return String.format("%,.2f", amount);
    }

    /**
     * Đóng popup Deposit.
     * closeButton là MFXButton với onAction="#handleClose" trong FXML.
     */
    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}