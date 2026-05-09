package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

public class DepositController {

    @FXML private Label        balanceLabel;
    @FXML private MFXTextField amountField;
    @FXML private Label        validationLabel;
    @FXML private MFXButton    depositButton;
    @FXML private MFXButton    btn10;
    @FXML private MFXButton    btn50;
    @FXML private MFXButton    btn100;
    @FXML private MFXButton    btn500;
    @FXML private MFXButton    btn1000;
    @FXML private MFXButton    btn2000;
    @FXML private MFXButton    btn5000;
    @FXML private MFXButton    btn10000;
    @FXML private ImageView    closeButton;

    private double currentBalance = 0;

    @FXML
    public void initialize() {
        validationLabel.setText("");
        currentBalance = 500.00;
        updateBalanceLabel();
        closeButton.setOnMouseClicked(this::handleClose);
    }

    @FXML
    private void onQuickAmount(MouseEvent e) {
        MFXButton clicked = (MFXButton) e.getSource();
        String text = clicked.getText().replace(" USD", "").replace(",", "");
        amountField.setText(text);
        validationLabel.setText("");
    }

    @FXML
    private void handleDeposit() {
        validationLabel.setStyle("-fx-text-fill: red;");
        validationLabel.setText("");

        String raw = amountField.getText().trim().replace(",", "");

        if (raw.isEmpty()) {
            validationLabel.setText("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(raw);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            validationLabel.setText("Invalid amount.");
            return;
        }

        if (amount < 10) {
            validationLabel.setText("Minimum deposit is 10.00 USD");
            return;
        }

        if (amount > 100000) {
            validationLabel.setText("Maximum deposit is 100,000.00 USD");
            return;
        }

        currentBalance += amount;
        updateBalanceLabel();
        amountField.setText("");
        validationLabel.setStyle("-fx-text-fill: green;");
        validationLabel.setText("Deposit successful! + " + formatMoney(amount) + " USD");
    }

    private void updateBalanceLabel() {
        balanceLabel.setText(formatMoney(currentBalance) + " USD");
    }

    private String formatMoney(double amount) {
        return String.format("%,.2f", amount);
    }

    @FXML
    private void handleClose(MouseEvent e) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}