//package com.example.auctionmanagementsystem.controller;
//
//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.input.MouseEvent;
//import io.github.palexdev.materialfx.controls.MFXButton;
//import io.github.palexdev.materialfx.controls.MFXTextField;
//import io.github.palexdev.materialfx.controls.MFXPasswordField;
//
///**
// * Controller cho View/auction_login.fxml
// */
//public class LoginController {
//
//    @FXML private MFXTextField     usernameField;
//    @FXML private MFXPasswordField passwordField;
//    @FXML private Label            unValidLabel;
//    @FXML private Label            pwValidLabel;
//    @FXML private MFXButton        loginButton;
//    @FXML private MFXButton        signupButton;      // top-bar
//    @FXML private MFXButton        forgotPassButton;
//
//    @FXML
//    public void initialize() {
//        // Ẩn error labels lúc khởi động
//        if (unValidLabel != null) unValidLabel.setVisible(false);
//        if (pwValidLabel != null) pwValidLabel.setVisible(false);
//
//        // Wire buttons bằng code (backup nếu FXML chưa có onAction)
//        if (signupButton   != null) signupButton.setOnAction(e   -> goToSignup());
//        if (forgotPassButton != null) forgotPassButton.setOnAction(e -> openForgotPassword());
//        if (loginButton    != null) loginButton.setOnAction(e    -> handleLogin());
//    }
//
//    // ── Gọi từ FXML: signupButton top-bar ────────────────────────────────────
//    @FXML
//    public void onSignupButtonClick() {
//        goToSignup();
//    }
//
//    // ── Gọi từ FXML: "Sign Up" label link trong form ─────────────────────────
//    @FXML
//    public void onSignupClick(MouseEvent event) {
//        goToSignup();
//    }
//
//    // ── Gọi từ FXML: loginButton ──────────────────────────────────────────────
//    @FXML
//    public void onLoginButtonClick() {
//        handleLogin();
//    }
//
//    // ── Gọi từ FXML: forgotPassButton ────────────────────────────────────────
//    @FXML
//    public void onForgotPassClick() {
//        openForgotPassword();
//    }
//
//    // ── Logic ─────────────────────────────────────────────────────────────────
//
//    private void handleLogin() {
//        // ── TEST MODE: login tự do, không cần credentials ────────────────────
//        // Xóa hoặc comment block này khi tích hợp DB thật
//
//        String username = (usernameField != null && !usernameField.getText().isBlank())
//                ? usernameField.getText().trim()
//                : "TestUser";
//
//        // Admin nếu gõ "admin", còn lại là user thường
//        boolean isAdmin = username.equalsIgnoreCase("admin");
//
//        SessionManager.getInstance().login(
//                isAdmin ? 0 : 1,
//                username,
//                username + "@demo.com",
//                isAdmin
//        );
//
//        if (isAdmin) {
//            NavigationUtil.goTo(loginButton, NavigationUtil.ADMIN);
//        } else {
//            NavigationUtil.goTo(loginButton, NavigationUtil.AUCTION_LIST);
//        }
//        // ── Kết thúc TEST MODE ────────────────────────────────────────────────
//    }
//
//    private void goToSignup() {
//        // Dùng loginButton làm source nếu signupButton null
//        javafx.scene.Node src = signupButton != null ? signupButton : loginButton;
//        NavigationUtil.goTo(src, NavigationUtil.SIGNUP);
//    }
//
//    private void openForgotPassword() {
//        javafx.scene.Node src = forgotPassButton != null ? forgotPassButton : loginButton;
//        NavigationUtil.openPopup(src, NavigationUtil.FORGOT_PASS, "Đặt lại mật khẩu");
//    }
//}

//Đây là chạy dưới quyền admin(dòng code này để test giao diện admin nhé)
package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;

public class LoginController {

    @FXML private MFXTextField     usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private Label            unValidLabel;
    @FXML private Label            pwValidLabel;
    @FXML private MFXButton        loginButton;
    @FXML private MFXButton        signupButton;
    @FXML private MFXButton        forgotPassButton;

    @FXML
    public void initialize() {
        if (unValidLabel != null) unValidLabel.setVisible(false);
        if (pwValidLabel != null) pwValidLabel.setVisible(false);

        if (signupButton     != null) signupButton.setOnAction(e     -> goToSignup());
        if (forgotPassButton != null) forgotPassButton.setOnAction(e -> openForgotPassword());
        if (loginButton      != null) loginButton.setOnAction(e      -> handleLogin());
    }

    @FXML
    public void onSignupButtonClick() {
        goToSignup();
    }

    @FXML
    public void onSignupClick(MouseEvent event) {
        goToSignup();
    }

    @FXML
    public void onLoginButtonClick() {
        handleLogin();
    }

    @FXML
    public void onForgotPassClick() {
        openForgotPassword();
    }

    private void handleLogin() {
        String username = (usernameField != null && !usernameField.getText().isBlank())
                ? usernameField.getText().trim()
                : "TestUser";

        // Tạm thời tất cả đều là admin để test
        // Sau khi kết nối DB thật thì đổi lại thành: username.equalsIgnoreCase("admin")
        boolean isAdmin = true;

        SessionManager.getInstance().login(
                isAdmin ? 0 : 1,
                username,
                username + "@demo.com",
                isAdmin
        );

        NavigationUtil.goTo(loginButton, NavigationUtil.AUCTION_LIST);
    }

    private void goToSignup() {
        javafx.scene.Node src = signupButton != null ? signupButton : loginButton;
        NavigationUtil.goTo(src, NavigationUtil.SIGNUP);
    }

    private void openForgotPassword() {
        javafx.scene.Node src = forgotPassButton != null ? forgotPassButton : loginButton;
        NavigationUtil.openPopup(src, NavigationUtil.FORGOT_PASS, "Đặt lại mật khẩu");
    }
}