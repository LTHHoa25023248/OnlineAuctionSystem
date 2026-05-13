package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.model.Admin;
import com.example.auctionmanagementsystem.model.Seller;
import com.example.auctionmanagementsystem.model.User;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

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
        hideError(unValidLabel);
        hideError(pwValidLabel);

        if (loginButton      != null) loginButton.setOnAction(e      -> handleLogin());
        if (signupButton     != null) signupButton.setOnAction(e     -> goToSignup());
        if (forgotPassButton != null) forgotPassButton.setOnAction(e -> openForgotPassword());

        if (passwordField != null) {
            passwordField.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ENTER) handleLogin();
            });
        }
    }

    @FXML public void onLoginButtonClick()         { handleLogin(); }
    @FXML public void onSignupButtonClick()        { goToSignup(); }
    @FXML public void onSignupClick(MouseEvent e)  { goToSignup(); }
    @FXML public void onForgotPassClick()          { openForgotPassword(); }

    private void handleLogin() {
        hideError(unValidLabel);
        hideError(pwValidLabel);

        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText() : "";

        if (username.isEmpty()) {
            showError(unValidLabel, "Vui lòng nhập tên đăng nhập.");
            return;
        }
        if (password.isEmpty()) {
            showError(pwValidLabel, "Vui lòng nhập mật khẩu.");
            return;
        }

        setLoginButtonState(false, "Đang đăng nhập...");

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() {
                return UserDAO.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            if (user != null) {
                String phone     = UserDAO.getStringField(user.getId(), "phone");
                String firstName = UserDAO.getStringField(user.getId(), "first_name");
                String lastName  = UserDAO.getStringField(user.getId(), "last_name");

                // Xác định role từ kiểu object trả về bởi UserDAO.login()
                String role = (user instanceof Admin)  ? "ADMIN"
                        : (user instanceof Seller) ? "SELLER"
                        : "BIDDER";

                SessionManager.getInstance().login(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        phone,
                        firstName,
                        lastName,
                        user instanceof Admin,
                        role
                );

                NavigationUtil.goTo(loginButton, NavigationUtil.AUCTION_LIST);
            } else {
                showError(unValidLabel, "Sai tên đăng nhập hoặc mật khẩu.");
                setLoginButtonState(true, "Login");
            }
        });

        loginTask.setOnFailed(e -> {
            showError(unValidLabel, "Lỗi kết nối. Vui lòng thử lại.");
            setLoginButtonState(true, "Login");
            loginTask.getException().printStackTrace();
        });

        new Thread(loginTask).start();
    }

    private void goToSignup() {
        Node src = (signupButton != null) ? signupButton : loginButton;
        NavigationUtil.goTo(src, NavigationUtil.SIGNUP);
    }

    private void openForgotPassword() {
        Node src = (forgotPassButton != null) ? forgotPassButton : loginButton;
        NavigationUtil.openPopup(src, NavigationUtil.FORGOT_PASS, "Reset Password");
    }

    private void showError(Label label, String message) {
        if (label == null) return;
        Platform.runLater(() -> {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        });
    }

    private void hideError(Label label) {
        if (label == null) return;
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private void setLoginButtonState(boolean enabled, String text) {
        Platform.runLater(() -> {
            if (loginButton != null) {
                loginButton.setDisable(!enabled);
                loginButton.setText(text);
            }
        });
    }
}