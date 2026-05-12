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
import java.util.concurrent.ThreadLocalRandom;

/**
 * ════════════════════════════════════════════════════════════
 * AddListingController
 * Popup thêm sản phẩm đấu giá mới — add_listing.fxml
 * Mở từ: AuctionListController → onSellButtonClick()
 * ════════════════════════════════════════════════════════════
 *
 * LUỒNG HOẠT ĐỘNG:
 *   1. User nhập thông tin (tên, category, giá, ngày, mô tả)
 *   2. User chọn ảnh qua FileChooser (không bắt buộc)
 *   3. User bấm "Add" → validate → tạo AuctionItem → đóng popup
 *   4. AuctionListController đọc lastAddedItem → hiện lên đầu danh sách
 *
 * ── CẦN GẮN DATABASE ──────────────────────────────────────
 *   handleAdd() → TODO: AuctionDAO.create(...)
 *   chooseImage() → TODO: ImageStorageService.save(file)
 *
 * ── HOẠT ĐỘNG ĐẦY ĐỦ ──────────────────────────────────────
 *   chooseImage()  → mở FileChooser, lưu path ✅
 *   validate form  → kiểm tra từng trường ✅
 *   tạo AuctionItem → truyền về AuctionListController ✅
 *   handleClose()  → đóng popup ✅
 */
public class AddListingController {

    // ── FXML fields ───────────────────────────────────────────────────────────
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

    /** File ảnh user đã chọn — null nếu chưa chọn */
    private File selectedImageFile;

    /**
     * [TRUYỀN DỮ LIỆU POPUP → MÀN HÌNH CHA]
     *
     * AuctionItem vừa được tạo trong handleAdd().
     * AuctionListController đọc field này ngay sau khi popup đóng.
     *
     * Static vì NavigationUtil.openPopup() không trả về controller
     * của AddListing trực tiếp.
     *
     * TODO (khi có DB):
     *   Xóa field này.
     *   handleAdd() gọi AuctionDAO.create() → lưu DB.
     *   AuctionListController chỉ cần loadListings() để thấy item mới.
     */
    public static AuctionListController.AuctionItem lastAddedItem = null;

    /**
     * [KHỞI TẠO — TỰ ĐỘNG GỌI KHI FXML LOAD]
     */
    @FXML
    public void initialize() {
        validationLabel.setText("");

        // Điền danh sách category vào dropdown
        category.getItems().addAll(
                "Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others");

        // Wire sự kiện
        chooseImageButton.setOnAction(e -> chooseImage());
        addButton.setOnAction(e         -> handleAdd());
        closeButton.setOnMouseClicked(this::handleClose);
    }

    /** Placeholder — không có logic, tránh click form đóng popup */
    @FXML
    private void onCloseButtonClick(MouseEvent e) { /* intentionally empty */ }

    /**
     * [HOẠT ĐỘNG ĐẦY ĐỦ ✅]
     *
     * Mở FileChooser chọn ảnh sản phẩm.
     * Cập nhật imageLabel với tên file đã chọn.
     *
     * TODO (khi có DB):
     *   Thay getAbsolutePath() bằng:
     *   String savedPath = ImageStorageService.copyToAppFolder(selectedImageFile);
     *   Để ảnh không mất khi file gốc bị xóa hoặc di chuyển.
     */
    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select product image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        Stage stage = (Stage) chooseImageButton.getScene().getWindow();
        selectedImageFile = fc.showOpenDialog(stage);

        if (selectedImageFile != null) {
            imageLabel.setText(selectedImageFile.getName());
        }
    }

    /**
     * [VALIDATE ✅ — LƯU DB CHƯA LÀM ❌]
     *
     * Validate form, tạo AuctionItem và lưu vào lastAddedItem.
     * AuctionListController đọc lastAddedItem sau khi popup đóng
     * để hiển thị ngay lên đầu danh sách.
     *
     * TODO (khi có DB — QUAN TRỌNG):
     *   Thay phần tạo AuctionItem bằng:
     *
     *   String imagePath = selectedImageFile != null
     *       ? ImageStorageService.copyToAppFolder(selectedImageFile)
     *       : null;
     *
     *   int newId = AuctionDAO.create(
     *       name.getText().trim(),
     *       category.getValue(),
     *       price,
     *       enddate.getValue(),
     *       description.getText().trim(),
     *       imagePath,
     *       SessionManager.getInstance().getUserId()
     *   );
     *
     *   Sau đó xóa lastAddedItem — không cần nữa vì DB đã có.
     */
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
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            validationLabel.setText("Invalid starting price.");
            return;
        }
        if (enddate.getValue() == null) {
            validationLabel.setText("Please select an end date.");
            return;
        }

        // ── Tạo AuctionItem để truyền về AuctionListController ────────────────
        // ID tạm thời ngẫu nhiên trong range cao để tránh trùng với sample data (1-100)
        int tempId = ThreadLocalRandom.current().nextInt(1000, 9999);

        // Lấy path ảnh nếu user đã chọn
        String imagePath = selectedImageFile != null
                ? selectedImageFile.getAbsolutePath()
                : null;

        // Tính số ngày còn lại từ hôm nay đến ngày kết thúc
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.now(), enddate.getValue());
        if (daysLeft < 1) daysLeft = 1; // tối thiểu 1 ngày

        // Tạo item mới — sẽ hiện lên đầu danh sách
        lastAddedItem = new AuctionListController.AuctionItem(
                tempId,
                name.getText().trim(),
                category.getValue(),
                price,
                0,              // 0 bids vì mới tạo
                (int) daysLeft,
                imagePath
        );

        System.out.println("[AddListing] Created: " + lastAddedItem.name
                + " | " + lastAddedItem.category
                + " | " + lastAddedItem.price + " USD"
                + " | Image: " + (imagePath != null ? imagePath : "none"));

        // TODO: AuctionDAO.create(name, category, price, enddate, description, imagePath, userId)

        // Đóng popup — AuctionListController sẽ đọc lastAddedItem ngay sau đây
        ((Stage) addButton.getScene().getWindow()).close();
    }

    /**
     * [HOẠT ĐỘNG ĐẦY ĐỦ ✅]
     * Đóng popup không lưu gì cả.
     */
    private void handleClose(MouseEvent e) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}