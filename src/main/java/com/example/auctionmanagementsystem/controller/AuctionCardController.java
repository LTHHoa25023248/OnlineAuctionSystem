package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.File;

public class AuctionCardController {

  @FXML
  private ImageView image;
  @FXML
  private Label title;
  @FXML
  private Label price;
  @FXML
  private Label totalBids;
  @FXML
  private Label timeLeft;
  @FXML
  private Label categoryLabel;
  @FXML
  private Label popularNowLabel;

  private AuctionListController parentController;
  private int auctionId;

  /**
   * [HOẠT ĐỘNG ĐẦY ĐỦ ✅]
   *
   * Nhận AuctionItem và điền dữ liệu vào card. Signature đổi từ (int, AuctionListController) →
   * (AuctionItem, AuctionListController) để nhận đủ thông tin thật từ item.
   *
   * @param item AuctionItem chứa toàn bộ thông tin sản phẩm
   * @param parent Reference về AuctionListController để callback khi click
   */
  public void setAuction(AuctionListController.AuctionItem item, AuctionListController parent) {
    this.auctionId = item.id;
    this.parentController = parent;

    title.setText(item.name);
    price.setText(String.format("%,.0f USD", item.price));
    totalBids.setText(item.bids + " bids");
    timeLeft.setText("Ending In: " + item.daysLeft + " day(s)");
    categoryLabel.setText(item.category);

    // Chỉ hiện "Popular Now" nếu có nhiều bid
    boolean isPopular = item.bids > 10;
    popularNowLabel.setText("Popular Now");
    popularNowLabel.setVisible(isPopular);
    popularNowLabel.setManaged(isPopular);
  }

  /**
   * [HOẠT ĐỘNG ĐẦY ĐỦ ✅]
   *
   * Load ảnh từ đường dẫn file tuyệt đối. null hoặc file không tồn tại → giữ placeholder, không
   * crash.
   *
   * @param imagePath Đường dẫn tuyệt đối đến file ảnh, null nếu chưa có ảnh
   *
   *        TODO (khi có DB): Nếu lưu relative path trong DB: File file = new
   *        File(AppConfig.IMAGE_FOLDER + imagePath);
   */
  public void setImage(String imagePath) {
    if (imagePath == null || imagePath.isBlank())
      return;

    try {
      File file = new File(imagePath);
      if (file.exists()) {
        Image img = new Image(file.toURI().toString());
        image.setImage(img);
        image.setPreserveRatio(true);
      } else {
        System.err.println("[AuctionCard] File not found: " + imagePath);
      }
    } catch (Exception e) {
      System.err.println("[AuctionCard] Cannot load image: " + imagePath);
    }
  }

  /**
   * [HOẠT ĐỘNG ĐẦY ĐỦ ✅] Click card → mở popup AuctionDetail. FXML cần:
   * onMouseClicked="#onCardClick" trên root container.
   */
  @FXML
  private void onCardClick(MouseEvent event) {
    if (parentController != null)
      parentController.openAuctionDetail(auctionId);
  }
}
