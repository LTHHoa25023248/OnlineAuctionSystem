package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.model.Admin;
import com.example.auctionmanagementsystem.model.Seller;
import com.example.auctionmanagementsystem.model.User;
import com.google.gson.JsonObject;
import java.util.Map;
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

  @FXML
  private MFXTextField usernameField;
  @FXML
  private MFXPasswordField passwordField;
  @FXML
  private Label unValidLabel;
  @FXML
  private Label pwValidLabel;
  @FXML
  private MFXButton loginButton;
  @FXML
  private MFXButton signupButton;
  @FXML
  private MFXButton forgotPassButton;

  @FXML
  public void initialize() {
    hideError(unValidLabel);
    hideError(pwValidLabel);

    if (loginButton != null)
      loginButton.setOnAction(e -> handleLogin());
    if (signupButton != null)
      signupButton.setOnAction(e -> goToSignup());
    if (forgotPassButton != null)
      forgotPassButton.setOnAction(e -> openForgotPassword());

    if (passwordField != null) {
      passwordField.setOnKeyPressed(e -> {
        if (e.getCode() == javafx.scene.input.KeyCode.ENTER)
          handleLogin();
      });
    }
  }

  @FXML
  public void onLoginButtonClick() {
    handleLogin();
  }

  @FXML
  public void onSignupButtonClick() {
    goToSignup();
  }

  @FXML
  public void onSignupClick(MouseEvent e) {
    goToSignup();
  }

  @FXML
  public void onForgotPassClick() {
    openForgotPassword();
  }

  private void handleLogin() {
    // System.out.println("[Client] Nút đăng nhập đã được bấm!");
    hideError(unValidLabel);
    hideError(pwValidLabel);

    String username = usernameField != null ? usernameField.getText().trim() : "";
    String password = passwordField != null ? passwordField.getText() : "";

    if (username.isEmpty()) {
      showError(unValidLabel, "Please enter your username.");
      return;
    }
    if (password.isEmpty()) {
      showError(pwValidLabel, "Please enter your password");
      return;
    }

    setLoginButtonState(false, "Logging in...");

    Task<JsonObject> loginTask = new Task<>() {
      @Override
      protected JsonObject call() throws Exception {
        return ApiClient.post("/auth/login",
            Map.of("username", username, "password", password));
      }
    };

    loginTask.setOnSucceeded(e -> {
      JsonObject resp = loginTask.getValue();
      if (resp.get("success").getAsBoolean()) {
        int userId       = resp.get("userId").getAsInt();
        String uname     = resp.get("username").getAsString();
        String email     = resp.get("email").getAsString();
        String phone     = resp.get("phone").getAsString();
        String firstName = resp.get("firstName").getAsString();
        String lastName  = resp.get("lastName").getAsString();
        String role      = resp.get("role").getAsString();
        boolean isAdmin  = resp.get("isAdmin").getAsBoolean();
        SessionManager.getInstance().login(userId, uname, email, phone,
            firstName, lastName, isAdmin, role);
        NavigationUtil.goTo(loginButton, NavigationUtil.AUCTION_LIST);
      } else {
        showError(unValidLabel, "Incorrect username or password.");
        setLoginButtonState(true, "Login");
      }
    });

    loginTask.setOnFailed(e -> {
      showError(unValidLabel, "Connection error. Please try again.");
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
    if (label == null)
      return;
    Platform.runLater(() -> {
      label.setText(message);
      label.setVisible(true);
      label.setManaged(true);
    });
  }

  private void hideError(Label label) {
    if (label == null)
      return;
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
