package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;

import java.io.File;

/**
 * Controller cho View/components/add_listing.fxml
 * Mở dưới dạng popup từ AuctionListController (sellButton).
 */
public class AddListingController {

    @FXML private MFXTextField        name;
    @FXML private MFXComboBox<String> category;
    @FXML private MFXTextField        startingprice;
    @FXML private MFXDatePicker       enddate;
    @FXML private MFXTextField        description;
    @FXML private MFXButton           chooseImageButton;
    @FXML private Label               imageLabel;
    @FXML private MFXButton           addButton;
    @FXML private Label               validationLabel;
    @FXML private ImageView           closeButton;

    private File selectedImageFile;

    @FXML
    public void initialize() {
        validationLabel.setText("");
        category.getItems().addAll("Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others");

        chooseImageButton.setOnAction(e -> chooseImage());
        addButton.setOnAction(e -> handleAdd());
        closeButton.setOnMouseClicked(this::handleClose);
    }

    /** Root AnchorPane có onMouseClicked="#onCloseButtonClick" — không làm gì cả
     *  để tránh đóng khi click bên trong form */
    @FXML
    private void onCloseButtonClick(MouseEvent e) { /* intentionally empty */ }

    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh sản phẩm");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png","*.jpg","*.jpeg","*.gif"));
        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        selectedImageFile = fc.showOpenDialog(stage);
        if (selectedImageFile != null)
            imageLabel.setText(selectedImageFile.getName());
    }

    private void handleAdd() {
        validationLabel.setText("");

        if (name.getText().trim().isEmpty()) {
            validationLabel.setText("Tiêu đề là bắt buộc."); return;
        }
        if (category.getValue() == null) {
            validationLabel.setText("Vui lòng chọn danh mục."); return;
        }
        double price;
        try {
            price = Double.parseDouble(startingprice.getText().trim());
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            validationLabel.setText("Giá khởi điểm không hợp lệ."); return;
        }
        if (enddate.getValue() == null) {
            validationLabel.setText("Vui lòng chọn ngày kết thúc."); return;
        }

        /* ── Thay bằng DAO thực ─────────────────────────────────────────────
         *   String imagePath = selectedImageFile != null
         *       ? ImageStorageService.save(selectedImageFile) : null;
         *   AuctionDAO.create(name.getText().trim(), category.getValue(),
         *       price, enddate.getValue(),
         *       description.getText().trim(), imagePath,
         *       SessionManager.getInstance().getUserId());
         * ─────────────────────────────────────────────────────────────────── */

        ((Stage) addButton.getScene().getWindow()).close();
    }

    private void handleClose(MouseEvent e) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}