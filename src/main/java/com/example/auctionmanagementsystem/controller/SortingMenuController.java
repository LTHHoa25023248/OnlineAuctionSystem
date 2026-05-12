package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXRadioButton;

/**
 * SortingMenuController — Popup chọn tiêu chí sắp xếp (sortingmenu.fxml).
 *
 * Mở từ AuctionListController (sortButton) dạng popup modal.
 * Sau khi đóng, caller đọc getSelectedSort() để biết tiêu chí user đã chọn.
 *
 * Các tiêu chí:
 *   radio1 → PRICE_DESC  (Giá cao → thấp)
 *   radio2 → PRICE_ASC   (Giá thấp → cao)
 *   radio3 → NEWEST      (Mới nhất) ← mặc định
 *   radio4 → OLDEST      (Cũ nhất)
 *   radio5 → ENDING_SOON (Sắp hết hạn)
 *   radio6 → ENDING_LATER(Còn nhiều thời gian)
 */
public class SortingMenuController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private MFXRadioButton radio1; // Price (High to Low)
    @FXML private MFXRadioButton radio2; // Price (Low to High)
    @FXML private MFXRadioButton radio3; // Newest
    @FXML private MFXRadioButton radio4; // Oldest
    @FXML private MFXRadioButton radio5; // Ending Soon
    @FXML private MFXRadioButton radio6; // Ending Later
    @FXML private ImageView      closeButton;

    private ToggleGroup toggleGroup;

    /** Giá trị sort được chọn — đọc từ bên ngoài sau khi popup đóng */
    private String selectedSort = "NEWEST";

    @FXML
    public void initialize() {
        // Nhóm các radio button lại — chỉ cho chọn 1 cái
        toggleGroup = new ToggleGroup();
        radio1.setToggleGroup(toggleGroup);
        radio2.setToggleGroup(toggleGroup);
        radio3.setToggleGroup(toggleGroup);
        radio4.setToggleGroup(toggleGroup);
        radio5.setToggleGroup(toggleGroup);
        radio6.setToggleGroup(toggleGroup);

        // Mặc định chọn "Newest"
        radio3.setSelected(true);

        if (closeButton != null)
            closeButton.setOnMouseClicked(this::handleClose);
    }

    /**
     * Đọc radio button được chọn, lưu vào selectedSort rồi đóng popup.
     * Được gọi từ nút "Apply" trong FXML (onAction="#applySort").
     */
    @FXML
    public void applySort() {
        if      (radio1.isSelected()) selectedSort = "PRICE_DESC";
        else if (radio2.isSelected()) selectedSort = "PRICE_ASC";
        else if (radio3.isSelected()) selectedSort = "NEWEST";
        else if (radio4.isSelected()) selectedSort = "OLDEST";
        else if (radio5.isSelected()) selectedSort = "ENDING_SOON";
        else if (radio6.isSelected()) selectedSort = "ENDING_LATER";

        // Đóng popup — caller sẽ gọi getSelectedSort() sau showAndWait()
        ((Stage) radio1.getScene().getWindow()).close();
    }

    /** Đóng popup không áp dụng sort */
    @FXML
    private void handleClose(MouseEvent e) {
        if (closeButton != null)
            ((Stage) closeButton.getScene().getWindow()).close();
    }

    /**
     * Trả về tiêu chí sort đã chọn.
     * Được đọc bởi AuctionListController sau khi popup đóng:
     *   SortingMenuController ctrl = NavigationUtil.openPopup(...);
     *   String sort = ctrl.getSelectedSort();
     */
    public String getSelectedSort() {
        return selectedSort;
    }
}