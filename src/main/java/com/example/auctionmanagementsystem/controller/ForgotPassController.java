package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.dao.UserDAO;
import com.example.auctionmanagementsystem.service.EmailService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * ForgotPassController — Popup đặt lại mật khẩu 3 bước (forgotpass.fxml).
 *
 * Luồng đầy đủ:
 *
 * Bước 1 — Nhập email → bấm "Send Code" • Validate email hợp lệ • Kiểm tra email có trong DB không
 * (UserDAO.emailExistsForReset) • Tạo OTP 6 chữ số (EmailService.generateCode) • Lưu OTP vào DB với
 * thời gian hết hạn (UserDAO.saveResetCode) • Gửi email chứa OTP (EmailService.sendResetCode) —
 * trên background thread • Vô hiệu hoá nút Send để tránh spam
 *
 * Bước 2 — Nhập mã OTP từ email vào codeField
 *
 * Bước 3 — Nhập mật khẩu mới → bấm "Save New Password" • Validate mật khẩu ≥ 8 ký tự, chứa chữ+số,
 * hai trường khớp • Xác thực OTP với DB (UserDAO.verifyResetCode) • Cập nhật mật khẩu + xóa OTP
 * (UserDAO.resetPassword) • Thông báo thành công → tự đóng popup sau 2 giây
 */
public class ForgotPassController {

  // ── FXML fields ───────────────────────────────────────────────────────────
  @FXML
  private MFXTextField emailField;
  @FXML
  private MFXTextField codeField;
  @FXML
  private MFXPasswordField passwordField;
  @FXML
  private MFXPasswordField confirmPasswordField;
  @FXML
  private MFXButton sendCodeButton;
  @FXML
  private MFXButton okButton;
  @FXML
  private MFXButton closeButton;
  @FXML
  private Label unValidLabel; // lỗi email / OTP
  @FXML
  private Label pwValidLabel; // lỗi password
  @FXML
  private Label label; // thông báo chung (xanh = OK, đỏ = lỗi)

  /** Email đã được xác nhận tồn tại trong DB — lưu để dùng ở bước 3 */
  private String verifiedEmail = null;

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    hideError(unValidLabel);
    hideError(pwValidLabel);
    clearStatus();

    // Wire backup (phòng khi FXML không khai báo onAction)
    if (sendCodeButton != null)
      sendCodeButton.setOnAction(e -> handleSendCode());
    if (okButton != null)
      okButton.setOnAction(e -> handleSave());
  }

  // ══════════════════════════════════════════════════════════════════════════
  // BƯỚC 1: Gửi OTP
  // ══════════════════════════════════════════════════════════════════════════

  /**
     * Validate email → kiểm tra DB → tạo và gửi OTP.
     * Chạy IO trên background thread.
     */
    @FXML
    private void handleSendCode() {
        hideError(unValidLabel);
        clearStatus();

        String email = emailField != null ? emailField.getText().trim() : "";

        // Validate định dạng email
        if (email.isEmpty()) {
            showError(unValidLabel, "Vui lòng nhập địa chỉ email.");
            return;
        }
        if (!isValidEmail(email)) {
            showError(unValidLabel, "Địa chỉ email không hợp lệ.");
            return;
        }

        setSendCodeButtonState(false, "Đang gửi...");

        Task<SendCodeResult> task = new Task<>() {
            @Override
            protected SendCodeResult call() {
                // Kiểm tra email có trong hệ thống không
                if (!UserDAO.emailExistsForReset(email)) {
                    return SendCodeResult.EMAIL_NOT_FOUND;
                }

                // Tạo OTP + tính thời gian hết hạn (10 phút)
                String code       = EmailService.generateCode();
                long   expiresAt  = System.currentTimeMillis() + EmailService.OTP_EXPIRE_MS;

                // Lưu vào DB
                boolean saved = UserDAO.saveResetCode(email, code, expiresAt);
                if (!saved) return SendCodeResult.DB_ERROR;

                // Gửi email
                boolean sent = EmailService.sendResetCode(email, code);
                if (!sent) return SendCodeResult.EMAIL_SEND_FAIL;

                return SendCodeResult.SUCCESS;
            }
        };

        task.setOnSucceeded(e -> {
            SendCodeResult result = task.getValue();
            switch (result) {
                case SUCCESS -> {
                    verifiedEmail = email;
                    showStatus("Code send to " + email
                            + ". Please check your inbox.", true);
                    // Disable nút 60 giây chống spam
                    startResendCountdown();
                }
                case EMAIL_NOT_FOUND -> {
                    showError(unValidLabel, "This email is not registered.");
                    setSendCodeButtonState(true, "Send Code");
                }
                case DB_ERROR -> {
                    showStatus("System error. Please try again", false);
                    setSendCodeButtonState(true, "Send Code");
                }
                case EMAIL_SEND_FAIL -> {
                    showStatus("Failed to send email.", false);
                    setSendCodeButtonState(true, "Send Code");
                }
            }
        });

        task.setOnFailed(e -> {
            showStatus("Connection error. Please try again", false);
            setSendCodeButtonState(true, "Send Code");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

  // ══════════════════════════════════════════════════════════════════════════
  // BƯỚC 3: Lưu mật khẩu mới
  // ══════════════════════════════════════════════════════════════════════════

  /**
     * Validate OTP + mật khẩu → cập nhật DB → thông báo + đóng popup.
     */
    @FXML
    private void handleSave() {
        hideError(unValidLabel);
        hideError(pwValidLabel);
        clearStatus();

        String code    = codeField            != null ? codeField.getText().trim()    : "";
        String pass    = passwordField        != null ? passwordField.getText()        : "";
        String confirm = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        // ── Validate client-side ──────────────────────────────────────────────
        if (verifiedEmail == null) {
            showError(unValidLabel, "Please send a verification code first.");
            return;
        }
        if (code.isEmpty()) {
            showError(unValidLabel, "Please enter the verification code");
            return;
        }
        if (code.length() != 6 || !code.matches("\\d+")) {
            showError(unValidLabel, "Verification code must be 6 ");
            return;
        }
        if (pass.length() < 8) {
            showError(pwValidLabel, "Password must be at least 8 characters."); return;
        }
        if (!pass.matches(".*[a-zA-Z].*") || !pass.matches(".*\\d.*")) {
            showError(pwValidLabel, "Password must contain both letters and numbers."); return;
        }
        if (!pass.equals(confirm)) {
            showError(pwValidLabel, "Passwords do not match."); return;
        }

        setOkButtonState(false, "Saving...");

        final String emailToReset = verifiedEmail;

        Task<ResetResult> task = new Task<>() {
            @Override
            protected ResetResult call() {
                boolean ok = UserDAO.resetPassword(emailToReset, code, pass);
                if (!ok) {
                    // Phân biệt: sai code hay DB lỗi
                    boolean codeValid = UserDAO.verifyResetCode(emailToReset, code);
                    return codeValid ? ResetResult.DB_ERROR : ResetResult.INVALID_CODE;
                }
                return ResetResult.SUCCESS;
            }
        };

        task.setOnSucceeded(e -> {
            ResetResult result = task.getValue();
            switch (result) {
                case SUCCESS -> {
                    showStatus("Password updated successfully!", true);
                    // Tự đóng popup sau 2 giây
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                        Platform.runLater(this::closePopup);
                    }).start();
                }
                case INVALID_CODE -> {
                    showError(unValidLabel, "Invalid or expired verification code.");
                    setOkButtonState(true, "Save New Password");
                }
                case DB_ERROR -> {
                    showStatus("System error. Please try again.", false);
                    setOkButtonState(true, "Save New Password");
                }
            }
        });

        task.setOnFailed(e -> {
            showStatus("Connection error. Please try again.", false);
            setOkButtonState(true, "Save New Password");
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

  // ── Đóng popup ────────────────────────────────────────────────────────────

  @FXML
  private void handleClose() {
    closePopup();
  }

  private void closePopup() {
    if (closeButton != null && closeButton.getScene() != null) {
      Stage stage = (Stage) closeButton.getScene().getWindow();
      stage.close();
    }
  }

  // ── Countdown chống spam gửi email ───────────────────────────────────────

  /**
   * Disable nút Send Code trong 60 giây, đếm ngược hiển thị trên button.
   */
  private void startResendCountdown() {
    new Thread(() -> {
      for (int i = 60; i >= 1; i--) {
        final int sec = i;
        Platform.runLater(() -> setSendCodeButtonText("Gửi lại sau " + sec + "s"));
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
          break;
        }
      }
      Platform.runLater(() -> setSendCodeButtonState(true, "Send Code"));
    }).start();
  }

  // ── Validation Helpers ────────────────────────────────────────────────────

  private boolean isValidEmail(String email) {
    return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  }

  // ── UI Helpers ────────────────────────────────────────────────────────────

  private void showError(Label lbl, String msg) {
    if (lbl == null)
      return;
    Platform.runLater(() -> {
      lbl.setText(msg);
      lbl.setStyle("-fx-text-fill: #FF6B6B;");
      lbl.setVisible(true);
      lbl.setManaged(true);
    });
  }

  private void hideError(Label lbl) {
    if (lbl == null)
      return;
    lbl.setText("");
    lbl.setVisible(false);
    lbl.setManaged(false);
  }

  private void showStatus(String msg, boolean isSuccess) {
    if (label == null)
      return;
    Platform.runLater(() -> {
      label.setText(msg);
      label.setStyle(isSuccess ? "-fx-text-fill: #3DBA7F;" : "-fx-text-fill: #FF6B6B;");
      label.setVisible(true);
      label.setManaged(true);
    });
  }

  private void clearStatus() {
    if (label == null)
      return;
    label.setText("");
    label.setVisible(false);
    label.setManaged(false);
  }

  private void setSendCodeButtonState(boolean enabled, String text) {
    Platform.runLater(() -> {
      if (sendCodeButton != null) {
        sendCodeButton.setDisable(!enabled);
        sendCodeButton.setText(text);
      }
    });
  }

  private void setSendCodeButtonText(String text) {
    if (sendCodeButton != null)
      sendCodeButton.setText(text);
  }

  private void setOkButtonState(boolean enabled, String text) {
    Platform.runLater(() -> {
      if (okButton != null) {
        okButton.setDisable(!enabled);
        okButton.setText(text);
      }
    });
  }

  // ── Inner enums ───────────────────────────────────────────────────────────

  private enum SendCodeResult {
    SUCCESS, EMAIL_NOT_FOUND, DB_ERROR, EMAIL_SEND_FAIL
  }
  private enum ResetResult {
    SUCCESS, INVALID_CODE, DB_ERROR
  }
}
