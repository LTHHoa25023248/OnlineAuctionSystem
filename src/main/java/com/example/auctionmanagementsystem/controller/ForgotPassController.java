package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * ForgotPassController — Popup đặt lại mật khẩu (forgotpass.fxml).
 *
 * Mở từ: LoginController → openForgotPassword() dạng popup modal.
 * Đóng: Bấm nút X trên top bar → đóng popup, trở về màn hình Login.
 *
 * Luồng 3 bước:
 *   Bước 1: Nhập email → bấm "Send Code"
 *   Bước 2: Nhập mã xác thực vào codeField
 *   Bước 3: Nhập mật khẩu mới → bấm "Save New Password"
 *
 * TODO: Tích hợp EmailService gửi code thật và UserDAO để reset password.
 */
public class ForgotPassController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private MFXTextField     emailField;
    @FXML private MFXTextField     codeField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXPasswordField confirmPasswordField;
    @FXML private MFXButton        sendCodeButton;
    @FXML private MFXButton        okButton;
    @FXML private MFXButton        closeButton;  // nút X đóng popup
    @FXML private Label            unValidLabel;
    @FXML private Label            pwValidLabel;
    @FXML private Label            label;

    @FXML
    public void initialize() {
        if (unValidLabel != null) { unValidLabel.setVisible(false); unValidLabel.setManaged(false); }
        if (pwValidLabel != null) { pwValidLabel.setVisible(false); pwValidLabel.setManaged(false); }
        if (label        != null)   label.setText("");

        if (sendCodeButton != null) sendCodeButton.setOnAction(e -> handleSendCode());
        if (okButton       != null) okButton.setOnAction(e       -> handleSave());
        // closeButton dùng onAction="#handleClose" trong FXML, không cần wire thêm
    }

    // ── Bước 1: Gửi mã xác thực ──────────────────────────────────────────────

    /**
     * Validate email rồi gửi mã xác thực.
     * TODO: Thay System.out.println bằng EmailService.sendCode(email).
     */
    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            show(unValidLabel, "Please enter a valid email.");
            return;
        }
        hide(unValidLabel);
        if (label != null) {
            label.setStyle("-fx-text-fill: #3DBA7F;");
            label.setText("Code sent to " + email);
        }
        System.out.println("[ForgotPass] Send code to: " + email);
    }

    // ── Bước 3: Lưu mật khẩu mới ─────────────────────────────────────────────

    /**
     * Validate mã xác thực và mật khẩu mới, sau đó reset.
     * TODO: Thay bằng UserDAO.resetPassword(code, newPassword).
     */
    @FXML
    private void handleSave() {
        if (label != null) label.setText("");

        String code    = codeField            != null ? codeField.getText().trim()    : "";
        String pass    = passwordField        != null ? passwordField.getText()        : "";
        String confirm = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (code.isEmpty()) {
            show(unValidLabel, "Please enter the verification code.");
            return;
        }
        if (pass.length() < 8) {
            show(pwValidLabel, "Password must be at least 8 characters.");
            return;
        }
        if (!pass.equals(confirm)) {
            show(pwValidLabel, "Passwords do not match.");
            return;
        }

        hide(unValidLabel);
        hide(pwValidLabel);

        if (label != null) {
            label.setStyle("-fx-text-fill: #3DBA7F;");
            label.setText("Password updated successfully!");
        }
        // TODO: UserDAO.resetPassword(code, pass);
    }

    /**
     * Đóng popup ForgotPass, trở về màn hình Login.
     * Gọi từ nút X trên top bar qua onAction="#handleClose" trong FXML.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void show(Label lbl, String msg) {
        if (lbl == null) return;
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void hide(Label lbl) {
        if (lbl == null) return;
        lbl.setText("");
        lbl.setVisible(false);
        lbl.setManaged(false);
    }
}