package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;

/**
 * Controller cho auction_signup.fxml
 *
 * Điều hướng:
 *   loginButton (top-bar)  → auction_login.fxml
 *   signupButton (submit)  → auction_login.fxml (sau khi đăng ký thành công)
 */
public class SignupController {

    @FXML private MFXTextField     firstNameField;
    @FXML private MFXTextField     lastNameField;
    @FXML private MFXTextField     usernameField;
    @FXML private MFXTextField     emailField;
    @FXML private MFXTextField     phoneField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXPasswordField confirmPasswordField;
    @FXML private MFXTextField     addressField;
    @FXML private CheckBox         termsCheckBox;

    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label usernameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label termsError;
    @FXML private Label generalError;

    @FXML private MFXButton loginButton;
    @FXML private MFXButton signupButton;

    @FXML
    public void initialize() {
        // loginButton dùng onAction="#onLoginButtonClick" trong FXML
    }

    @FXML
    private void onLoginButtonClick() {
        NavigationUtil.goTo(loginButton, NavigationUtil.LOGIN);
    }

    @FXML
    private void onSignupButtonClick() {
        if (!validateForm()) return;

        /* ── Thay bằng DAO thực ─────────────────────────────────────────────
         *   UserDAO.register(
         *       firstNameField.getText(), lastNameField.getText(),
         *       usernameField.getText(),  emailField.getText(),
         *       phoneField.getText(),     passwordField.getText(),
         *       addressField.getText());
         * ─────────────────────────────────────────────────────────────────── */

        NavigationUtil.goTo(signupButton, NavigationUtil.LOGIN);
    }

    @FXML
    private void onTermsClick(MouseEvent event) {
        // TODO: mở dialog điều khoản
        System.out.println("Terms clicked");
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateForm() {
        clearErrors();
        boolean ok = true;

        if (firstNameField.getText().trim().isEmpty()) {
            show(firstNameError, "First name là bắt buộc."); ok = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            show(lastNameError, "Last name là bắt buộc."); ok = false;
        }
        if (usernameField.getText().trim().isEmpty()) {
            show(usernameError, "Username là bắt buộc."); ok = false;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            show(emailError, "Email không hợp lệ."); ok = false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            show(phoneError, "Số điện thoại là bắt buộc."); ok = false;
        }
        if (passwordField.getText().length() < 8) {
            show(passwordError, "Mật khẩu phải có ít nhất 8 ký tự."); ok = false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            show(confirmPasswordError, "Mật khẩu không khớp."); ok = false;
        }
        if (!termsCheckBox.isSelected()) {
            show(termsError, "Bạn phải đồng ý điều khoản."); ok = false;
        }
        return ok;
    }

    private void show(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
    }

    private void clearErrors() {
        Label[] all = {firstNameError, lastNameError, usernameError, emailError,
                phoneError, passwordError, confirmPasswordError, termsError, generalError};
        for (Label l : all) { l.setText(""); l.setVisible(false); }
    }
}