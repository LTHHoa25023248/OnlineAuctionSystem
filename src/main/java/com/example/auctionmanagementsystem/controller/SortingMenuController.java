package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import io.github.palexdev.materialfx.controls.MFXRadioButton;
import javafx.scene.control.ToggleGroup;

public class SortingMenuController {

    @FXML private MFXRadioButton radio1; // Price (High to Low)
    @FXML private MFXRadioButton radio2; // Price (Low to High)
    @FXML private MFXRadioButton radio3; // Newest
    @FXML private MFXRadioButton radio4; // Oldest
    @FXML private MFXRadioButton radio5; // Ending Soon
    @FXML private MFXRadioButton radio6; // Ending Later
    @FXML private ImageView      closeButton;

    private ToggleGroup toggleGroup;
    private String selectedSort = "NEWEST";

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        radio1.setToggleGroup(toggleGroup);
        radio2.setToggleGroup(toggleGroup);
        radio3.setToggleGroup(toggleGroup);
        radio4.setToggleGroup(toggleGroup);
        radio5.setToggleGroup(toggleGroup);
        radio6.setToggleGroup(toggleGroup);

        // Mặc định chọn Newest
        radio3.setSelected(true);

        if (closeButton != null)
            closeButton.setOnMouseClicked(this::handleClose);
    }

    @FXML
    public void applySort() {
        if (radio1.isSelected())      selectedSort = "PRICE_DESC";
        else if (radio2.isSelected()) selectedSort = "PRICE_ASC";
        else if (radio3.isSelected()) selectedSort = "NEWEST";
        else if (radio4.isSelected()) selectedSort = "OLDEST";
        else if (radio5.isSelected()) selectedSort = "ENDING_SOON";
        else if (radio6.isSelected()) selectedSort = "ENDING_LATER";

        ((Stage) radio1.getScene().getWindow()).close();
    }

    @FXML
    private void handleClose(MouseEvent e) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        if (stage != null) stage.close();
    }

    public String getSelectedSort() {
        return selectedSort;
    }
}