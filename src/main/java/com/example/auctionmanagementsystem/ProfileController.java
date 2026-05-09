package com.example.auctionmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPaginatedTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import com.example.auctionmanagementsystem.controller.SessionManager;
import com.example.auctionmanagementsystem.controller.NavigationUtil;

public class ProfileController {

    @FXML private Label                 name;
    @FXML private MFXTextField          nameField;
    @FXML private MFXTextField          usernameField;
    @FXML private MFXTextField          emailField;
    @FXML private MFXTextField          phoneField;
    @FXML private MFXButton             editButton;
    @FXML private MFXButton             closeButton;
    @FXML private MFXButton             depositButton;
    @FXML private MFXPaginatedTableView table;

    private boolean editMode = false;

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();

        name.setText(session.getUsername());
        nameField.setText(session.getUsername());
        usernameField.setText(session.getUsername());
        emailField.setText(session.getEmail());

        setEditable(false);
        editButton.setOnAction(e -> toggleEdit());
        depositButton.setOnAction(e -> onDepositButtonClick());
    }

    @FXML
    private void onDepositButtonClick() {
        NavigationUtil.openPopup(depositButton, NavigationUtil.DEPOSIT, "Deposit");
    }

    @FXML
    private void onCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void toggleEdit() {
        editMode = !editMode;
        setEditable(editMode);
        editButton.setText(editMode ? "Save" : "Edit Profile");
        if (!editMode) saveProfile();
    }

    private void setEditable(boolean editable) {
        nameField.setAllowEdit(editable);
        usernameField.setAllowEdit(editable);
        emailField.setAllowEdit(editable);
        phoneField.setAllowEdit(editable);
    }

    private void saveProfile() {
        name.setText(nameField.getText());
    }
}