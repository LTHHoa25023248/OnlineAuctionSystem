package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.model.*;
import com.example.auctionmanagementsystem.service.AuctionService;
import com.example.auctionmanagementsystem.service.ImageStorageService;
import com.example.auctionmanagementsystem.service.ItemService;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
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
  private MFXTextField endHour;
  @FXML
  private MFXTextField endMinute;
  @FXML
  private MFXTextField endSecond;
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
  @FXML private ComboBox<String> cbCategory;
  @FXML private VBox dynamicAttributesBox;


  private File selectedImageFile;
  public java.util.Map<String, MFXTextField> currentAttributeFields = new java.util.HashMap<>();
  public static AuctionListController.AuctionItem lastAddedItem = null;

  @FXML
  public void initialize() {
    validationLabel.setText("");
    category.getItems().addAll("Electronics", "Art", "Vehicle");
    category.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      renderDynamicFields(newVal);
    });

    chooseImageButton.setOnAction(e -> chooseImage());
    addButton.setOnAction(e -> handleAdd());
    closeButton.setOnMouseClicked(this::handleClose);
  }

  // Quyết định danh mụ nào sẽ hiện ra attributes cần
  private void renderDynamicFields(String cat) {
    dynamicAttributesBox.getChildren().clear();
    currentAttributeFields.clear();

    if (cat == null) return;

    switch (cat) {
      case "Electronics" -> {
        createInput("brand", "Brand (e.g., Apple)");
        createInput("warranty", "Warranty (Months)");
      }
      case "Art" -> {
        createInput("artist", "Artist Name");
        createInput("material", "Material (e.g., Oil, Paper)");
        createInput("theme", "Theme (e.g., Landscape)");
      }
      case "Vehicle" -> {
        createInput("year", "Year (e.g., 2022)");
        createInput("mileage", "Mileage (KM)");
      }
    }
  }

  private void createInput(String key, String promptText) {
    MFXTextField txt = new MFXTextField();
    // Bật hiệu ứng chữ nổi lên viền
    txt.setFloatMode(io.github.palexdev.materialfx.enums.FloatMode.BORDER);
    txt.setFloatingText("  " + promptText + "  ");
    txt.setPrefHeight(45.0); // Chỉnh cao xíu cho cân đối
    txt.setPrefWidth(354.0);
    txt.getStyleClass().add("fields"); 

    dynamicAttributesBox.getChildren().add(txt);
    currentAttributeFields.put(key, txt); // Cất vào Map để xài lúc Save
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

    int hour = 23, minute = 59, second = 0;
    try {
      String hText = endHour != null ? endHour.getText().trim() : "";
      String mText = endMinute != null ? endMinute.getText().trim() : "";
      String sText = endSecond != null ? endSecond.getText().trim() : "";
    } catch (NumberFormatException ex) {
      validationLabel.setText("Invalid time. HH 0-23, MM/SS 0-59");
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
      String itemType = category.getValue().toUpperCase();
      for (Map.Entry<String, MFXTextField> entry : currentAttributeFields.entrySet()) {
          String key = entry.getKey();
          String val = entry.getValue().getText().trim();
          
          // Nếu người dùng lười không nhập, mình set mặc định
          if (val.isEmpty()) {
              if (key.equals("warranty") || key.equals("year") || key.equals("mileage")) {
                  val = "0"; 
              } else {
                  val = "Unknown";
              }
          }
          attributes.put(key, val);
      }

      LocalDateTime endDateTime = enddate.getValue().atTime(hour, minute, second);
      Map<String, Object> body = new HashMap<>();
      body.put("sellerId",     SessionManager.getInstance().getUserId());
      body.put("itemType",     itemType);
      body.put("name",         name.getText().trim());
      body.put("desc",         description.getText().trim());
      body.put("price",        price);
      body.put("imageFileName",savedFileName);
      body.put("endTime",      endDateTime.toString());
      body.put("attributes",   attributes);
      JsonObject resp = ApiClient.post("/auction/create", body);
      if (!resp.get("success").getAsBoolean())
        throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "Create failed");

      int auctionId = resp.get("auctionId").getAsInt();

      // Dung id thuc tu DB thay vi id tam thoi ngau nhien nhu truoc
      lastAddedItem = new AuctionListController.AuctionItem( auctionId, name.getText().trim(),category.getValue(), price, 0, (int) daysLeft, displayImagePath);
      lastAddedItem.setAttributes(attributes); // full path de JavaFX hien thi duoc
      System.out.println("[AddListing] Saved to API: " + lastAddedItem.name+ " | auctionId=" + auctionId + " | image=" + (savedFileName != null ? savedFileName : "none"));

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
