package com.example.auctionmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXPasswordField;

import java.net.URL;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    @FXML private MFXTextField firstNameField;
    @FXML private MFXTextField lastNameField;
    @FXML private MFXTextField usernameField;
    @FXML private MFXTextField emailField;
    @FXML private MFXTextField phoneField;
    @FXML private MFXTextField addressField;
    @FXML private MFXPasswordField passwordField;
    @FXML private MFXPasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;

    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label usernameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label termsError;
    @FXML private Label generalError;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @FXML
    public void onSignupButtonClick(ActionEvent e) {
        clearErrors();
        if (!validate()) return;

        // TODO: Gọi service để đăng ký tài khoản
        String username  = usernameField.getText().trim();
        String email     = emailField.getText().trim();
        String password  = passwordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String phone     = phoneField.getText().trim();
        String address   = addressField.getText().trim();

        System.out.println("Registering: " + username + " / " + email);
        // TODO: navigate to login or home after success
    }

    @FXML
    public void onLoginButtonClick(ActionEvent e) {
        // TODO: navigate to login screen
    }

    @FXML
    public void onTermsClick(MouseEvent e) {
        // TODO: show terms dialog
    }

    private boolean validate() {
        boolean valid = true;

        if (firstNameField.getText().trim().isEmpty()) {
            showError(firstNameError, "First name is required");
            valid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError(lastNameError, "Last name is required");
            valid = false;
        }
        if (usernameField.getText().trim().isEmpty()) {
            showError(usernameError, "Username is required");
            valid = false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showError(emailError, "Email is required");
            valid = false;
        } else if (!emailField.getText().contains("@")) {
            showError(emailError, "Invalid email address");
            valid = false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showError(phoneError, "Phone number is required");
            valid = false;
        }
        if (passwordField.getText().length() < 8) {
            showError(passwordError, "Password must be at least 8 characters");
            valid = false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError(confirmPasswordError, "Passwords do not match");
            valid = false;
        }
        if (!termsCheckBox.isSelected()) {
            showError(termsError, "You must agree to the Terms & Conditions");
            valid = false;
        }
        return valid;
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private void clearErrors() {
        Label[] errors = {
                firstNameError, lastNameError, usernameError,
                emailError, phoneError, passwordError,
                confirmPasswordError, termsError, generalError
        };
        for (Label l : errors) {
            l.setText("");
            l.setVisible(false);
        }
    }
}
