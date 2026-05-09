package com.example.auctionmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import com.example.auctionmanagementsystem.controller.NavigationUtil;

/**
 * Controller cho View/forgotpass.fxml
 * Mở dưới dạng popup từ LoginController.
 */
public class ForgotPassController {

    @FXML private MFXTextField     emailField;
    @FXML private MFXTextField     codeField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXPasswordField confirmPasswordField;
    @FXML private MFXButton        sendCodeButton;
    @FXML private MFXButton        okButton;
    @FXML private MFXButton        signupButton;
    @FXML private Label            unValidLabel;
    @FXML private Label            pwValidLabel;
    @FXML private Label            label;

    @FXML
    public void initialize() {
        unValidLabel.setVisible(false);
        pwValidLabel.setVisible(false);
        label.setText("");

        sendCodeButton.setOnAction(e -> handleSendCode());
        okButton.setOnAction(e -> handleSave());
        signupButton.setOnAction(e -> {
            closePopup();
            // LoginController sẽ chưa điều hướng; user tự bấm Sign Up ở Login
        });
    }

    private void handleSendCode() {
        unValidLabel.setVisible(false);
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            unValidLabel.setText("Nhập email hợp lệ.");
            unValidLabel.setVisible(true);
            return;
        }
        /* ── Thay bằng service gửi email thực ──────────────────────────────
         *   VerificationService.sendCode(email);
         * ─────────────────────────────────────────────────────────────────── */
        label.setText("Mã xác nhận đã gửi tới " + email);
    }

    private void handleSave() {
        pwValidLabel.setVisible(false);
        label.setText("");

        String code    = codeField.getText().trim();
        String pw      = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (code.isEmpty()) {
            label.setText("Vui lòng nhập mã xác nhận.");
            return;
        }
        if (pw.length() < 8) {
            pwValidLabel.setText("Mật khẩu phải có ít nhất 8 ký tự.");
            pwValidLabel.setVisible(true);
            return;
        }
        if (!pw.equals(confirm)) {
            pwValidLabel.setText("Mật khẩu không khớp.");
            pwValidLabel.setVisible(true);
            return;
        }
        /* ── Thay bằng DAO thực ─────────────────────────────────────────────
         *   boolean ok = VerificationService.verify(emailField.getText(), code);
         *   if (!ok) { label.setText("Mã không đúng."); return; }
         *   UserDAO.updatePassword(emailField.getText(), pw);
         * ─────────────────────────────────────────────────────────────────── */
        label.setText("Đổi mật khẩu thành công!");
        closePopup();
    }

    private void closePopup() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
}
