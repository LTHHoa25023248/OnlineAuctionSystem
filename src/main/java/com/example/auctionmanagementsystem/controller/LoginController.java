package com.example.auctionmanagementsystem.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private MFXTextField usernameField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXButton loginButton;
    @FXML private MFXButton signupButton;
    @FXML private MFXButton forgotPassButton;
    @FXML private Label unValidLabel;
    @FXML private Label pwValidLabel;
    @FXML private Label statListings;
    @FXML private Label statMembers;
    @FXML private Label statBids;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set stat labels (tránh ký tự đặc biệt trong FXML)
        statListings.setText("1,200+");
        statMembers.setText("5,800+");
        statBids.setText("$2M+");

        // Ẩn error labels lúc khởi tạo
        unValidLabel.setVisible(false);
        pwValidLabel.setVisible(false);

        // Xóa lỗi khi user bắt đầu gõ
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            unValidLabel.setVisible(false);
        });
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            pwValidLabel.setVisible(false);
        });
    }

    // ═══════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════
    @FXML
    public void onLoginButtonClick(ActionEvent e) {
        clearErrors();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!validate(username, password)) return;

        // TODO: Thay bằng logic xác thực thực tế với database
        boolean loginSuccess = checkCredentials(username, password);

        if (loginSuccess) {
            navigateTo("auction_list.fxml", loginButton);
        } else {
            showError(unValidLabel, "Invalid username or password");
        }
    }

    // ═══════════════════════════════════════════
    // SIGNUP BUTTON (top bar)
    // ═══════════════════════════════════════════
    @FXML
    public void onSignupButtonClick(ActionEvent e) {
        navigateTo("auction_signup.fxml", signupButton);
    }

    // ═══════════════════════════════════════════
    // SIGNUP LINK (bottom of form)
    // ═══════════════════════════════════════════
    @FXML
    public void onSignupClick(MouseEvent e) {
        navigateTo("auction_signup.fxml", signupButton);
    }

    // ═══════════════════════════════════════════
    // FORGOT PASSWORD
    // ═══════════════════════════════════════════
    @FXML
    public void onForgotPassButtonClick(ActionEvent e) {
        navigateTo("forgotpass.fxml", forgotPassButton);
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════
    private boolean validate(String username, String password) {
        boolean valid = true;

        if (username.isEmpty()) {
            showError(unValidLabel, "Username cannot be empty");
            valid = false;
        }
        if (password.isEmpty()) {
            showError(pwValidLabel, "Password cannot be empty");
            valid = false;
        }
        return valid;
    }

    private boolean checkCredentials(String username, String password) {
        // TODO: Thay bằng query database thực tế
        // Ví dụ tạm thời để test:
        return username.equals("admin") && password.equals("admin123");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private void clearErrors() {
        unValidLabel.setVisible(false);
        pwValidLabel.setVisible(false);
        unValidLabel.setText("");
        pwValidLabel.setText("");
    }

    private void navigateTo(String fxmlFile, javafx.scene.Node source) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/auctionmanagementsystem/View/" + fxmlFile
                    )
            );
            Parent root = loader.load();
            Stage stage = (Stage) source.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}