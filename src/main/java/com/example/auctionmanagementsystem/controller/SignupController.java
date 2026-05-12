package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * SignupController — Điều khiển màn hình đăng ký (auction_signup.fxml).
 *
 * Luồng:
 *   User điền form → bấm "Create Account"
 *   → validateForm() kiểm tra từng trường
 *   → Nếu hợp lệ: gọi UserDAO.register() → chuyển về màn hình Login
 *
 * Mỗi trường input đều có một error label riêng (firstNameError, emailError...).
 * Error label dùng managed="false" khi ẩn để không chiếm không gian layout.
 */
public class SignupController {

    // ── FXML input fields ─────────────────────────────────────────────────────
    @FXML private MFXTextField     firstNameField;
    @FXML private MFXTextField     lastNameField;
    @FXML private MFXTextField     usernameField;
    @FXML private MFXTextField     emailField;
    @FXML private MFXTextField     phoneField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXPasswordField confirmPasswordField;
    @FXML private MFXTextField     addressField;
    @FXML private CheckBox         termsCheckBox;

    // ── Error labels (hiển thị lỗi ngay bên dưới mỗi field) ─────────────────
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label usernameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label termsError;
    @FXML private Label generalError; // lỗi tổng quát (ít dùng)

    // ── Buttons ───────────────────────────────────────────────────────────────
    @FXML private MFXButton loginButton;  // top-bar: về trang Login
    @FXML private MFXButton signupButton; // submit form

    @FXML
    public void initialize() {
        // Sự kiện đã khai báo onAction trong FXML, không cần wire thêm
    }

    /** Gọi từ nút "Log In" trên top bar */
    @FXML
    private void onLoginButtonClick() {
        NavigationUtil.goTo(loginButton, NavigationUtil.LOGIN);
    }

    /**
     * Gọi khi bấm "Create Account".
     * Chỉ tiếp tục nếu form hợp lệ.
     * TODO: Thay comment bằng UserDAO.register() khi có DB.
     */
    @FXML
    private void onSignupButtonClick() {
        if (!validateForm()) return;

        // TODO: UserDAO.register(
        //     firstNameField.getText(), lastNameField.getText(),
        //     usernameField.getText(),  emailField.getText(),
        //     phoneField.getText(),     passwordField.getText(),
        //     addressField.getText());

        // Sau khi đăng ký thành công → về trang Login
        NavigationUtil.goTo(signupButton, NavigationUtil.LOGIN);
    }

    /** Gọi khi click vào "Terms & Conditions" */
    @FXML
    private void onTermsClick(MouseEvent event) {
        // TODO: Mở dialog điều khoản sử dụng
        System.out.println("[Signup] Terms clicked");
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Kiểm tra toàn bộ form.
     * Chạy qua tất cả trường, hiển thị lỗi ngay cả khi có nhiều trường sai.
     *
     * @return true nếu tất cả hợp lệ, false nếu có ít nhất 1 lỗi
     */
    private boolean validateForm() {
        clearErrors(); // xóa lỗi cũ trước
        boolean ok = true;

        if (firstNameField.getText().trim().isEmpty()) {
            show(firstNameError, "First name is required."); ok = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            show(lastNameError, "Last name is required."); ok = false;
        }
        if (usernameField.getText().trim().isEmpty()) {
            show(usernameError, "Username is required."); ok = false;
        }

        // Kiểm tra email có dấu @ tối thiểu
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            show(emailError, "Invalid email."); ok = false;
        }

        if (phoneField.getText().trim().isEmpty()) {
            show(phoneError, "Phone number is required."); ok = false;
        }
        if (passwordField.getText().length() < 8) {
            show(passwordError, "Password must be at least 8 characters."); ok = false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            show(confirmPasswordError, "Passwords do not match."); ok = false;
        }
        if (!termsCheckBox.isSelected()) {
            show(termsError, "You must agree to the terms."); ok = false;
        }
        return ok;
    }

    /** Hiển thị error label với message */
    private void show(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true); // chiếm không gian layout
    }

    /** Ẩn tất cả error labels và xóa text */
    private void clearErrors() {
        Label[] all = {
                firstNameError, lastNameError, usernameError, emailError,
                phoneError, passwordError, confirmPasswordError, termsError, generalError
        };
        for (Label l : all) {
            if (l != null) { l.setText(""); l.setVisible(false); l.setManaged(false); }
        }
    }
}