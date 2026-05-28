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

/**
 * AddListingController — Popup thêm sản phẩm đấu giá mới (add_listing.fxml)
 * Mở từ: AuctionListController → onSellButtonClick()
 *
 * LUỒNG HOẠT ĐỘNG:
 * 1. User nhập thông tin (tên, category, giá, ngày, mô tả)
 * 2. User chọn ảnh qua FileChooser (không bắt buộc)
 * 3. User bấm "Add" → validate → copy ảnh → lưu Item+Auction vào DB → đóng popup
 * 4. AuctionListController đọc lastAddedItem → hiện lên đầu danh sách
 */
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

  /** File ảnh user đã chọn — null nếu chưa chọn */
  private File selectedImageFile;

  /**
   * [TRUYỀN DỮ LIỆU POPUP → MÀN HÌNH CHA]
   *
   * AuctionItem vừa được tạo trong handleAdd(). AuctionListController đọc field này ngay sau khi
   * popup đóng.
   *
   * Static vì NavigationUtil.openPopup() không trả về controller của AddListing trực tiếp.
   *
   * TODO (khi có DB): Xóa field này. handleAdd() gọi AuctionDAO.create() → lưu DB.
   * AuctionListController chỉ cần loadListings() để thấy item mới.
   */
  public static AuctionListController.AuctionItem lastAddedItem = null;

  /**
   * [KHỞI TẠO — TỰ ĐỘNG GỌI KHI FXML LOAD]
   */
  @FXML
  public void initialize() {
    validationLabel.setText("");

    // Điền danh sách category vào dropdown
    category.getItems().addAll("Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others");

    // Wire sự kiện
    chooseImageButton.setOnAction(e -> chooseImage());
    addButton.setOnAction(e -> handleAdd());
    closeButton.setOnMouseClicked(this::handleClose);
  }

  /** Placeholder — không có logic, tránh click form đóng popup */
  @FXML
  private void onCloseButtonClick(MouseEvent e) { /* intentionally empty */ }

  /**
   * [HOẠT ĐỘNG ĐẦY ĐỦ ✅]
   *
   * Mở FileChooser chọn ảnh sản phẩm. Cập nhật imageLabel với tên file đã chọn.
   *
   * TODO (khi có DB): Thay getAbsolutePath() bằng: String savedPath =
   * ImageStorageService.copyToAppFolder(selectedImageFile); Để ảnh không mất khi file gốc bị xóa
   * hoặc di chuyển.
   */
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

    // ── Validate từng trường ──────────────────────────────────────────────
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

    // Tinh so ngay con lai tu hom nay den ngay ket thuc
    long daysLeft = java.time.temporal.ChronoUnit.DAYS
        .between(java.time.LocalDate.now(), enddate.getValue());
    if (daysLeft < 1) daysLeft = 1;

    try {
      // ── Bước 1: Copy ảnh vào thư mục app ─────────────────────────────
      // Luu ten file (UUID) vao DB thay vi path goc — anh khong bi mat khi file goc bi xoa
      String savedFileName = null;
      String displayImagePath = null;
      if (selectedImageFile != null) {
        savedFileName = ImageStorageService.save(selectedImageFile);
        displayImagePath = ImageStorageService.getFullPath(savedFileName);
      }

      // ── Bước 2: Map category UI → item_type DB ────────────────────────
      // Cars → VEHICLE, Fine Art → ART, con lai → ELECTRONICS
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

      // ── Bước 3: Tạo Item và lưu DB ──────────────────────────────────
      Item item = ItemFactory.createItem(itemType, name.getText().trim(),
          description.getText().trim(), price, attributes);
      item.setImagePath(savedFileName); // chi luu ten file vao DB, khong luu full path

      ItemService itemService = new ItemService();
      int itemId = itemService.createItem(item); // [FIX] creatItem → createItem (ten method da doi)
      item.setId(itemId);

      // ── Bước 4: Tạo Auction và lưu DB ──────────────────────────────
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

  /**
   * [HOẠT ĐỘNG ĐẦY ĐỦ ✅] Đóng popup không lưu gì cả.
   */
  private void handleClose(MouseEvent e) {
    ((Stage) closeButton.getScene().getWindow()).close();
  }
}
