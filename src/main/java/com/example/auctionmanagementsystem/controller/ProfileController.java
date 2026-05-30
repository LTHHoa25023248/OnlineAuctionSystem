package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.google.gson.JsonObject;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class ProfileController {

  @FXML private Label        name;
  @FXML private Label        balanceLabel;
  @FXML private MFXTextField nameField;
  @FXML private MFXTextField usernameField;
  @FXML private MFXTextField emailField;
  @FXML private MFXTextField phoneField;
  @FXML private MFXButton    editButton;
  @FXML private MFXButton    closeButton;
  @FXML private MFXButton    depositButton; // giữ field để FXML không lỗi

  private boolean editMode = false;

  @FXML
  public void initialize() {
    SessionManager session = SessionManager.getInstance();

    String fullName     = session.getFullName();
    String displayName  = fullName.isEmpty() ? session.getUsername() : fullName;

    if (name         != null) name.setText(displayName);
    if (nameField    != null) nameField.setText(displayName);
    if (usernameField!= null) usernameField.setText(session.getUsername());
    if (emailField   != null) emailField.setText(session.getEmail());
    if (phoneField   != null) phoneField.setText(session.getPhone());

    setEditable(false);

    if (editButton    != null) editButton.setOnAction(e -> toggleEdit());
    if (depositButton != null) {
      depositButton.setDisable(true);
      depositButton.setText("Unavailable");
    }

    loadBalance();
  }

  private void loadBalance() {
    int userId = SessionManager.getInstance().getUserId();
    Task<Double> task = new Task<>() {
      @Override
      protected Double call() throws Exception {
        JsonObject resp = ApiClient.getObject("/user/balance?userId=" + userId);
        return resp.get("balance").getAsDouble();
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      if (balanceLabel != null)
        balanceLabel.setText(String.format("%,.2f USD", task.getValue()));
    }));
    task.setOnFailed(e -> task.getException().printStackTrace());
    new Thread(task).start();
  }

  @FXML
  private void onDepositButtonClick() {
    // Deposit feature đã bị xóa
    if (depositButton != null) {
      depositButton.setDisable(true);
      depositButton.setText("Unavailable");
    }
  }

  @FXML
  private void onCloseButtonClick() {
    if (closeButton != null) {
      Stage stage = (Stage) closeButton.getScene().getWindow();
      stage.close();
    }
  }

  private void toggleEdit() {
    editMode = !editMode;
    setEditable(editMode);
    if (editButton != null) editButton.setText(editMode ? "Save" : "Edit Profile");
    if (!editMode) saveProfile();
  }

  private void setEditable(boolean editable) {
    if (nameField    != null) nameField.setAllowEdit(editable);
    if (usernameField!= null) usernameField.setAllowEdit(false); // username không cho sửa
    if (emailField   != null) emailField.setAllowEdit(false);    // email không cho sửa
    if (phoneField   != null) phoneField.setAllowEdit(editable);
  }

  /** Lưu firstName và phone vào DB. */
  private void saveProfile() {
    if (name != null && nameField != null) name.setText(nameField.getText());

    String displayName = nameField != null ? nameField.getText().trim() : "";
    String phone       = phoneField != null ? phoneField.getText().trim() : "";
    int    userId      = SessionManager.getInstance().getUserId();

    // Tách firstName / lastName từ displayName (nếu có khoảng trắng)
    String firstName, lastName;
    int spaceIdx = displayName.indexOf(' ');
    if (spaceIdx > 0) {
      firstName = displayName.substring(0, spaceIdx).trim();
      lastName  = displayName.substring(spaceIdx + 1).trim();
    } else {
      firstName = displayName;
      lastName  = "";
    }

    final String fn = firstName, ln = lastName, ph = phone;

    Task<Boolean> task = new Task<>() {
      @Override
      protected Boolean call() throws Exception {
        JsonObject resp = ApiClient.post("/user/profile", Map.of(
            "userId", userId, "firstName", fn, "lastName", ln, "phone", ph));
        return resp.get("success").getAsBoolean();
      }
    };

    task.setOnSucceeded(e -> {
      if (Boolean.TRUE.equals(task.getValue()))
        System.out.println("[Profile] Đã lưu thông tin.");
    });
    task.setOnFailed(e -> task.getException().printStackTrace());
    new Thread(task).start();
  }
}
