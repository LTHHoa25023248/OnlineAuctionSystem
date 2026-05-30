package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.google.gson.JsonObject;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class SignupController {

  @FXML
  private ToggleButton bidderToggle;
  @FXML
  private ToggleButton sellerToggle;

  @FXML
  private MFXTextField firstNameField;
  @FXML
  private MFXTextField lastNameField;
  @FXML
  private MFXTextField usernameField;
  @FXML
  private MFXTextField emailField;
  @FXML
  private MFXTextField phoneField;
  @FXML
  private MFXPasswordField passwordField;
  @FXML
  private MFXPasswordField confirmPasswordField;
  @FXML
  private MFXTextField addressField;
  @FXML
  private CheckBox termsCheckBox;

  @FXML
  private Label firstNameError;
  @FXML
  private Label lastNameError;
  @FXML
  private Label usernameError;
  @FXML
  private Label emailError;
  @FXML
  private Label phoneError;
  @FXML
  private Label passwordError;
  @FXML
  private Label confirmPasswordError;
  @FXML
  private Label termsError;
  @FXML
  private Label generalError;

  @FXML
  private MFXButton loginButton;
  @FXML
  private MFXButton signupButton;

  @FXML
  public void initialize() {
    if (signupButton != null)
      signupButton.setOnAction(e -> onSignupButtonClick());
    if (loginButton != null)
      loginButton.setOnAction(e -> onLoginButtonClick());

    if (bidderToggle != null) {
      bidderToggle.setOnAction(e -> {
        bidderToggle.setSelected(true);
        sellerToggle.setSelected(false);
      });
    }
    if (sellerToggle != null) {
      sellerToggle.setOnAction(e -> {
        sellerToggle.setSelected(true);
        bidderToggle.setSelected(false);
      });
    }
  }

  @FXML
  private void onRoleToggle() {}

  private boolean isSeller() {
    return sellerToggle != null && sellerToggle.isSelected();
  }

  @FXML
  private void onLoginButtonClick() {
    NavigationUtil.goTo(loginButton, NavigationUtil.LOGIN);
  }

  @FXML
    private void onSignupButtonClick() {
        if (!validateForm()) return;

        setSignupButtonState(false, "Registering...");
        clearGeneralError();

        final String firstName = firstNameField.getText().trim();
        final String lastName  = lastNameField.getText().trim();
        final String username  = usernameField.getText().trim();
        final String email     = emailField.getText().trim();
        final String phone     = phoneField.getText().trim();
        final String password  = passwordField.getText();
        final String address   = addressField.getText() != null ? addressField.getText().trim() : "";
        final boolean seller   = isSeller();

        Task<RegisterResult> task = new Task<>() {
            @Override
            protected RegisterResult call() throws Exception {
                JsonObject res = ApiClient.post("/auth/register", Map.of(
                        "firstName", firstName, "lastName", lastName,
                        "username", username, "email", email,
                        "phone", phone, "password", password,
                        "address", address, "seller", seller));
                return switch (res.get("result").getAsString()) {
                    case "SUCCESS"        -> RegisterResult.SUCCESS;
                    case "USERNAME_TAKEN" -> RegisterResult.USERNAME_TAKEN;
                    case "EMAIL_TAKEN"    -> RegisterResult.EMAIL_TAKEN;
                    default               -> RegisterResult.DB_ERROR;
                };
            }
        };

        task.setOnSucceeded(e -> {
            switch (task.getValue()) {
                case SUCCESS        -> NavigationUtil.goTo(signupButton, NavigationUtil.LOGIN);
                case USERNAME_TAKEN -> {
                    show(usernameError, "Username is already taken.");
                    setSignupButtonState(true, "Create Account");
                }
                case EMAIL_TAKEN    -> {
                    show(emailError, "Email is already registered.");
                    setSignupButtonState(true, "Create Account");
                }
                case DB_ERROR       -> {
                    showGeneralError("Registration failed. Please try again.");
                    setSignupButtonState(true, "Create Account");
                }
            }
        });

        task.setOnFailed(e -> {
            showGeneralError("Connection error. Please try again.");
            setSignupButtonState(true, "Create Account");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

  @FXML
  private void onTermsClick(MouseEvent event) {}

  private boolean validateForm() {
    clearErrors();
    boolean ok = true;

    if (firstNameField.getText().trim().isEmpty()) {
      show(firstNameError, "Please enter your first name.");
      ok = false;
    }
    if (lastNameField.getText().trim().isEmpty()) {
      show(lastNameError, "Please enter your last name.");
      ok = false;
    }

    String username = usernameField.getText().trim();
    if (username.isEmpty()) {
      show(usernameError, "Please enter a username.");
      ok = false;
    } else if (username.length() < 4) {
      show(usernameError, "Username must be at least 4 characters.");
      ok = false;
    } else if (username.contains(" ")) {
      show(usernameError, "Username must not contain spaces.");
      ok = false;
    }

    String email = emailField.getText().trim();
    if (email.isEmpty()) {
      show(emailError, "Please enter your email.");
      ok = false;
    } else if (!isValidEmail(email)) {
      show(emailError, "Invalid email address.");
      ok = false;
    }

    String phone = phoneField.getText().trim();
    if (phone.isEmpty()) {
      show(phoneError, "Please enter a phone number.");
      ok = false;
    } else if (!phone.matches("[+\\d\\s\\-()]+")) {
      show(phoneError, "Invalid phone number.");
      ok = false;
    }

    String pass = passwordField.getText();
    if (pass.length() < 8) {
      show(passwordError, "Password must be at least 8 characters.");
      ok = false;
    } else if (!pass.matches(".*[a-zA-Z].*") || !pass.matches(".*\\d.*")) {
      show(passwordError, "Password must contain both letters and numbers.");
      ok = false;
    }

    if (!pass.equals(confirmPasswordField.getText())) {
      show(confirmPasswordError, "Passwords do not match.");
      ok = false;
    }

    if (termsCheckBox != null && !termsCheckBox.isSelected()) {
      show(termsError, "You must agree to the terms of service.");
      ok = false;
    }

    return ok;
  }

  private boolean isValidEmail(String email) {
    return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  }

  private void show(Label label, String msg) {
    if (label == null)
      return;
    label.setText(msg);
    label.setVisible(true);
    label.setManaged(true);
  }

  private void clearErrors() {
    Label[] all = {firstNameError, lastNameError, usernameError, emailError, phoneError,
        passwordError, confirmPasswordError, termsError, generalError};
    for (Label l : all) {
      if (l != null) {
        l.setText("");
        l.setVisible(false);
        l.setManaged(false);
      }
    }
  }

  private void showGeneralError(String msg) {
    Platform.runLater(() -> {
      if (generalError != null) {
        generalError.setText(msg);
        generalError.setStyle("-fx-text-fill: #FF6B6B;");
        generalError.setVisible(true);
        generalError.setManaged(true);
      }
    });
  }

  private void clearGeneralError() {
    if (generalError != null) {
      generalError.setText("");
      generalError.setVisible(false);
      generalError.setManaged(false);
    }
  }

  private void setSignupButtonState(boolean enabled, String text) {
    Platform.runLater(() -> {
      if (signupButton != null) {
        signupButton.setDisable(!enabled);
        signupButton.setText(text);
      }
    });
  }

  private enum RegisterResult {
    SUCCESS, USERNAME_TAKEN, EMAIL_TAKEN, DB_ERROR
  }
}
