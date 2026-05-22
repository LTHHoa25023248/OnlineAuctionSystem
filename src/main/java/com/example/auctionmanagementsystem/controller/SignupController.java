package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.dao.UserDAO;
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

        setSignupButtonState(false, "Đang đăng ký...");
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
            protected RegisterResult call() {
                if (UserDAO.usernameExists(username)) return RegisterResult.USERNAME_TAKEN;
                if (UserDAO.emailExists(email))       return RegisterResult.EMAIL_TAKEN;

                boolean success = seller
                        ? UserDAO.registerSeller(firstName, lastName, username, email, phone, password, address)
                        : UserDAO.register(firstName, lastName, username, email, phone, password, address);

                return success ? RegisterResult.SUCCESS : RegisterResult.DB_ERROR;
            }
        };

        task.setOnSucceeded(e -> {
            switch (task.getValue()) {
                case SUCCESS        -> NavigationUtil.goTo(signupButton, NavigationUtil.LOGIN);
                case USERNAME_TAKEN -> {
                    show(usernameError, "Tên đăng nhập đã được sử dụng.");
                    setSignupButtonState(true, "Create Account");
                }
                case EMAIL_TAKEN    -> {
                    show(emailError, "Email đã được đăng ký.");
                    setSignupButtonState(true, "Create Account");
                }
                case DB_ERROR       -> {
                    showGeneralError("Đăng ký thất bại. Vui lòng thử lại sau.");
                    setSignupButtonState(true, "Create Account");
                }
            }
        });

        task.setOnFailed(e -> {
            showGeneralError("Lỗi kết nối. Vui lòng kiểm tra lại.");
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
      show(firstNameError, "Vui lòng nhập họ.");
      ok = false;
    }
    if (lastNameField.getText().trim().isEmpty()) {
      show(lastNameError, "Vui lòng nhập tên.");
      ok = false;
    }

    String username = usernameField.getText().trim();
    if (username.isEmpty()) {
      show(usernameError, "Vui lòng nhập tên đăng nhập.");
      ok = false;
    } else if (username.length() < 4) {
      show(usernameError, "Tên đăng nhập phải có ít nhất 4 ký tự.");
      ok = false;
    } else if (username.contains(" ")) {
      show(usernameError, "Tên đăng nhập không được chứa khoảng trắng.");
      ok = false;
    }

    String email = emailField.getText().trim();
    if (email.isEmpty()) {
      show(emailError, "Vui lòng nhập email.");
      ok = false;
    } else if (!isValidEmail(email)) {
      show(emailError, "Email không hợp lệ.");
      ok = false;
    }

    String phone = phoneField.getText().trim();
    if (phone.isEmpty()) {
      show(phoneError, "Vui lòng nhập số điện thoại.");
      ok = false;
    } else if (!phone.matches("[+\\d\\s\\-()]+")) {
      show(phoneError, "Số điện thoại không hợp lệ.");
      ok = false;
    }

    String pass = passwordField.getText();
    if (pass.length() < 8) {
      show(passwordError, "Mật khẩu phải có ít nhất 8 ký tự.");
      ok = false;
    } else if (!pass.matches(".*[a-zA-Z].*") || !pass.matches(".*\\d.*")) {
      show(passwordError, "Mật khẩu phải có cả chữ cái và chữ số.");
      ok = false;
    }

    if (!pass.equals(confirmPasswordField.getText())) {
      show(confirmPasswordError, "Mật khẩu xác nhận không khớp.");
      ok = false;
    }

    if (termsCheckBox != null && !termsCheckBox.isSelected()) {
      show(termsError, "Bạn phải đồng ý với điều khoản sử dụng.");
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
