package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;
import com.example.auctionmanagementsystem.service.AuctionService;
import com.example.auctionmanagementsystem.service.ImageStorageService;
import com.example.auctionmanagementsystem.service.ItemService;
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
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class AddListingController {

  // ── FXML fields ───────────────────────────────────────────────────────────
  @FXML
  private MFXTextField name;
  @FXML
  private MFXComboBox<String> category;
  @FXML
  private MFXTextField startingprice;
  @FXML
  private MFXDatePicker enddate;
  @FXML
  private MFXTextField description;
  @FXML
  private MFXButton chooseImageButton;
  @FXML
  private Label imageLabel;
  @FXML
  private MFXButton addButton;
  @FXML
  private Label validationLabel;
  @FXML
  private ImageView closeButton;


  private File selectedImageFile;
  public static AuctionListController.AuctionItem lastAddedItem = null;

  @FXML
  public void initialize() {
    validationLabel.setText("");
    category.getItems().addAll("Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others");

    chooseImageButton.setOnAction(e -> chooseImage());
    addButton.setOnAction(e -> handleAdd());
    closeButton.setOnMouseClicked(this::handleClose);
  }

  @FXML
  private void onCloseButtonClick(MouseEvent e) { /* intentionally empty */ }

  private void chooseImage() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Select product image");
    fc.getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

    Stage stage = (Stage) chooseImageButton.getScene().getWindow();
    selectedImageFile = fc.showOpenDialog(stage);

    if (selectedImageFile != null) {
      imageLabel.setText(selectedImageFile.getName());
    }
  }

  private void handleAdd() {
    validationLabel.setText("");


    if (name.getText().trim().isEmpty()) {
      validationLabel.setText("Title is required.");
      return;
    }
    if (category.getValue() == null) {
      validationLabel.setText("Please select a category.");
      return;
    }
    double price;
    try {
      price = Double.parseDouble(startingprice.getText().trim());
      if (price <= 0)
        throw new NumberFormatException();
    } catch (NumberFormatException ex) {
      validationLabel.setText("Invalid starting price.");
      return;
    }
    if (enddate.getValue() == null) {
      validationLabel.setText("Please select an end date.");
      return;
    }

    long daysLeft = java.time.temporal.ChronoUnit.DAYS
        .between(java.time.LocalDate.now(), enddate.getValue());
    if (daysLeft < 1) daysLeft = 1;

    try {
      String savedFileName = null;
      String displayImagePath = null;
      if (selectedImageFile != null) {
        savedFileName = ImageStorageService.save(selectedImageFile);
        displayImagePath = ImageStorageService.getFullPath(savedFileName);
      }

      Map<String, String> attributes = new HashMap<>();
      String itemType;
      switch (category.getValue()) {
        case "Cars" -> {
          itemType = "VEHICLE";
          attributes.put("year", "0");
          attributes.put("mileage", "0.0");
        }
        case "Fine Art" -> {
          itemType = "ART";
          attributes.put("artist", "Unknown");
          attributes.put("theme", "Unknown");
          attributes.put("material", "Unknown");
        }
        default -> {
          itemType = "ELECTRONICS";
          attributes.put("brand", category.getValue());
          attributes.put("warranty", "0");
        }
      }
      Item item = ItemFactory.createItem(itemType, name.getText().trim(),
          description.getText().trim(), price, attributes);
      item.setImagePath(savedFileName); // chi luu ten file vao DB, khong luu full path

      ItemService itemService = new ItemService();
      int itemId = itemService.createItem(item); // [FIX] creatItem → createItem (ten method da doi)
      item.setId(itemId);


      // Seller chi can id, khong can load toan bo thong tin tu DB
      Seller seller = new Seller();
      seller.setId(SessionManager.getInstance().getUserId());

      Auction auction = new Auction(item, seller, price, AuctionStatus.PENDING,
          LocalDateTime.now(), enddate.getValue().atTime(23, 59));

      Connection connect = DatabaseConnection.getConnection();
      connect.setAutoCommit(false);
      try {
        new AuctionService().creatAuction(connect, auction);
        connect.commit();
      } catch (Exception ex) {
        connect.rollback();
        throw ex;
      } finally {
        connect.close();
      }

      // ── Bước 5: Cập nhật lastAddedItem để AuctionListController hiển thị ngay ──
      // Dung id thuc tu DB thay vi id tam thoi ngau nhien nhu truoc
      lastAddedItem = new AuctionListController.AuctionItem(
          auction.getId(),
          name.getText().trim(),
          category.getValue(),
          price,
          0,             // 0 bids vì mới tạo
          (int) daysLeft,
          displayImagePath); // full path de JavaFX hien thi duoc

      System.out.println("[AddListing] Saved to DB: " + lastAddedItem.name
          + " | auctionId=" + auction.getId()
          + " | image=" + (savedFileName != null ? savedFileName : "none"));

    } catch (Exception ex) {
      ex.printStackTrace();
      validationLabel.setText("Lỗi: " + ex.getMessage());
      return;
    }

    // Dong popup — AuctionListController se doc lastAddedItem ngay sau day
    ((Stage) addButton.getScene().getWindow()).close();
  }

  private void handleClose(MouseEvent e) {
    ((Stage) closeButton.getScene().getWindow()).close();
  }
}
