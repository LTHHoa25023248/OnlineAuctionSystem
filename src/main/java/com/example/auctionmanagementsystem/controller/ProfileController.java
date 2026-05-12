package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPaginatedTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * ProfileController — Điều khiển màn hình hồ sơ (auction_profile.fxml).
 *
 * Màn hình mở dưới dạng popup từ AuctionListController (profileButton).
 *
 * Tính năng:
 *   - Hiển thị thông tin user từ SessionManager
 *   - Toggle chế độ Edit/View để sửa thông tin
 *   - Mở popup Deposit để nạp tiền
 *
 * TODO: Khi có DB, thay SessionManager bằng UserDAO.getById(userId)
 *       và lưu thay đổi bằng UserDAO.update().
 */
public class ProfileController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private Label                 name;          // Tên hiển thị lớn ở left panel
    @FXML private MFXTextField          nameField;     // Input full name
    @FXML private MFXTextField          usernameField; // Input username (thường read-only)
    @FXML private MFXTextField          emailField;    // Input email
    @FXML private MFXTextField          phoneField;    // Input phone
    @FXML private MFXButton             editButton;    // Toggle Edit / Save
    @FXML private MFXButton             closeButton;   // Đóng popup
    @FXML private MFXButton             depositButton; // Mở popup Deposit
    @FXML private MFXPaginatedTableView table;         // (Dự phòng) bảng lịch sử giao dịch

    /** true khi đang ở chế độ chỉnh sửa */
    private boolean editMode = false;

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();

        // Điền dữ liệu từ session hiện tại vào form
        name.setText(session.getUsername());
        nameField.setText(session.getUsername());
        usernameField.setText(session.getUsername());
        emailField.setText(session.getEmail());
        // phoneField để trống vì SessionManager chưa lưu số điện thoại

        // Mặc định: chỉ xem, không chỉnh sửa
        setEditable(false);

        // Wire sự kiện
        editButton.setOnAction(e    -> toggleEdit());
        depositButton.setOnAction(e -> onDepositButtonClick());
    }

    /**
     * Mở popup Deposit để nạp tiền vào ví.
     * Được gọi từ cả editButton (onAction FXML) và depositButton.
     */
    @FXML
    private void onDepositButtonClick() {
        NavigationUtil.openPopup(depositButton, NavigationUtil.DEPOSIT, "Deposit");
    }

    /**
     * Đóng popup Profile.
     * Gọi từ closeButton qua onMouseClicked trong FXML.
     */
    @FXML
    private void onCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Toggle giữa chế độ View và Edit.
     * Khi chuyển từ Edit → View, gọi saveProfile() để lưu thay đổi.
     */
    private void toggleEdit() {
        editMode = !editMode;
        setEditable(editMode);
        editButton.setText(editMode ? "Save" : "Edit Profile");
        if (!editMode) saveProfile(); // lưu khi thoát chế độ edit
    }

    /**
     * Bật/tắt khả năng chỉnh sửa cho tất cả input fields.
     *
     * @param editable true → có thể gõ vào fields
     */
    private void setEditable(boolean editable) {
        nameField.setAllowEdit(editable);
        usernameField.setAllowEdit(editable);
        emailField.setAllowEdit(editable);
        phoneField.setAllowEdit(editable);
    }

    /**
     * Lưu thay đổi profile.
     * Hiện tại chỉ cập nhật label name trên UI.
     * TODO: Thay bằng UserDAO.update(userId, nameField.getText(), ...).
     */
    private void saveProfile() {
        name.setText(nameField.getText());
        // TODO: UserDAO.update(...)
    }
}