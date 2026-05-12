package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * LoginController — Điều khiển màn hình đăng nhập (auction_login.fxml).
 *
 * Luồng chính:
 *   User nhập username + password → bấm Login
 *   → handleLogin() xác thực → lưu session → chuyển sang AuctionList
 *
 * Chú ý: Hiện tại chạy ở TEST MODE (isAdmin = true luôn).
 * Khi kết nối DB thật, thay phần handleLogin() bằng UserDAO.login().
 */
public class LoginController {

    // ── FXML fields — phải khớp fx:id trong auction_login.fxml ───────────────
    @FXML private MFXTextField     usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private Label            unValidLabel;    // hiển thị lỗi username
    @FXML private Label            pwValidLabel;    // hiển thị lỗi password
    @FXML private MFXButton        loginButton;
    @FXML private MFXButton        signupButton;    // nút "Sign Up" trên top bar
    @FXML private MFXButton        forgotPassButton;

    @FXML
    public void initialize() {
        // Ẩn error labels lúc khởi động
        if (unValidLabel     != null) unValidLabel.setVisible(false);
        if (pwValidLabel     != null) pwValidLabel.setVisible(false);

        // Wire sự kiện bằng code (backup nếu FXML chưa khai báo onAction)
        if (signupButton     != null) signupButton.setOnAction(e     -> goToSignup());
        if (forgotPassButton != null) forgotPassButton.setOnAction(e -> openForgotPassword());
        if (loginButton      != null) loginButton.setOnAction(e      -> handleLogin());
    }

    // ── Handlers gọi từ FXML (onAction / onMouseClicked) ─────────────────────

    /** Gọi từ nút Sign Up trên top bar */
    @FXML
    public void onSignupButtonClick() { goToSignup(); }

    /** Gọi từ Label "Sign Up" dạng link trong form (onMouseClicked) */
    @FXML
    public void onSignupClick(MouseEvent event) { goToSignup(); }

    /** Gọi từ nút Login chính */
    @FXML
    public void onLoginButtonClick() { handleLogin(); }

    /** Gọi từ nút "Forgot Password?" */
    @FXML
    public void onForgotPassClick() { openForgotPassword(); }

    // ── Business logic ────────────────────────────────────────────────────────

    /**
     * Xử lý đăng nhập.
     *
     * TEST MODE: Mọi user đều là admin, không cần password đúng.
     * PRODUCTION: Thay bằng:
     *   User user = UserDAO.login(username, password);
     *   if (user == null) { showError("Sai thông tin"); return; }
     *   SessionManager.getInstance().login(user.getId(), user.getUsername(),
     *       user.getEmail(), user.isAdmin());
     */
    private void handleLogin() {
        String username = (usernameField != null && !usernameField.getText().isBlank())
                ? usernameField.getText().trim()
                : "TestUser";

        // TODO: Đổi thành username.equalsIgnoreCase("admin") khi có DB
        boolean isAdmin = true;

        SessionManager.getInstance().login(
                isAdmin ? 0 : 1,
                username,
                username + "@demo.com",
                isAdmin
        );

        NavigationUtil.goTo(loginButton, NavigationUtil.AUCTION_LIST);
    }

    /** Chuyển sang màn hình đăng ký */
    private void goToSignup() {
        // Dùng signupButton nếu có, fallback sang loginButton nếu null
        Node src = signupButton != null ? signupButton : loginButton;
        NavigationUtil.goTo(src, NavigationUtil.SIGNUP);
    }

    /** Mở popup đặt lại mật khẩu */
    private void openForgotPassword() {
        Node src = forgotPassButton != null ? forgotPassButton : loginButton;
        NavigationUtil.openPopup(src, NavigationUtil.FORGOT_PASS, "Reset Password");
    }
}