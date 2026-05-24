package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

/**
 * AuctionDetailController — Popup chi tiết đấu giá (auction_detail.fxml).
 * Mở từ AuctionListController. Caller gọi loadAuction(id) sau khi popup load.
 */
public class AuctionDetailController {

  // ── FXML fields — thông tin auction ──────────────────────────────────────
  @FXML
  private ImageView image;
  @FXML
  private Label titleLabel;
  @FXML
  private Label priceLabel;
  @FXML
  private Label totalBidsLabel;
  @FXML
  private Label startLabel;
  @FXML
  private Label endLabel;
  @FXML
  private Label categoryLabel;
  @FXML
  private Label activeLabel; // badge "Active"
  @FXML
  private Label popularNowLabel; // badge "Popular Now"
  @FXML
  private Label userLabel; // người đăng auction
  @FXML
  private Label descriptionLabel;
  @FXML
  private Label winnerLabel; // hiện sau khi kết thúc
  @FXML
  private Label bidValidationLabel; // lỗi / thành công khi bid

  // ── FXML fields — bid section ─────────────────────────────────────────────
  @FXML
  private MFXTextField bidField;
  @FXML
  private MFXButton placeBidButton;
  @FXML
  private MFXButton endnowButton; // chỉ hiện với admin

  // closeButton là ImageView (không phải MFXButton) — dùng onMouseClicked
  @FXML
  private ImageView closeButton;

  /** ID auction đang xem */
  private int currentAuctionId;

  @FXML
  public void initialize() {
    bidValidationLabel.setText("");
    winnerLabel.setText("");
    endnowButton.setVisible(false);

    placeBidButton.setOnAction(e -> handlePlaceBid());
    endnowButton.setOnAction(e -> handleEndNow());
    closeButton.setOnMouseClicked(this::handleClose);
  }

  /**
   * Điền dữ liệu auction vào UI. Được gọi bởi AuctionListController sau khi popup load xong.
   *
   * @param auctionId ID của auction cần hiển thị
   */
  public void loadAuction(int auctionId) {
    this.currentAuctionId = auctionId;

    // TODO: Thay bằng dữ liệu thật từ AuctionDAO.getById(auctionId)
    titleLabel.setText("Auction Item #" + auctionId);
    priceLabel.setText("250.00 USD");
    totalBidsLabel.setText("7");
    startLabel.setText("01/05/2025");
    endLabel.setText("31/05/2025");
    categoryLabel.setText("Jewelry");
    activeLabel.setText("Active");
    popularNowLabel.setText("Popular Now");
    userLabel.setText(SessionManager.getInstance().getUsername());
    descriptionLabel
        .setText("This is a premium auction item. Bidding is open to all registered members.");

    // Nút "End Now" chỉ hiện với admin
    endnowButton.setVisible(SessionManager.getInstance().isAdmin());
  }

  // ── Bid handling ──────────────────────────────────────────────────────────

  /**
   * Đặt giá đấu. Validate số tiền nhập vào, hiển thị thông báo thành công/lỗi. TODO: Gọi
   * BidDAO.placeBid(auctionId, userId, amount) khi có DB.
   */
  private void handlePlaceBid() {
    bidValidationLabel.setStyle("-fx-text-fill: #E05454;");
    bidValidationLabel.setText("");

    String raw = bidField.getText().trim();
    if (raw.isEmpty()) {
      bidValidationLabel.setText("Please enter a bid amount.");
      return;
    }
    try {
      double bid = Double.parseDouble(raw);
      if (bid <= 0)
        throw new NumberFormatException();
      // Bid hợp lệ
      bidValidationLabel.setStyle("-fx-text-fill: #3DBA7F;");
      bidValidationLabel.setText("Bid of " + String.format("%.2f", bid) + " USD placed!");
      bidField.setText("");
      // TODO: BidDAO.placeBid(currentAuctionId, SessionManager.getInstance().getUserId(), bid);
    } catch (NumberFormatException ex) {
      bidValidationLabel.setText("Invalid amount.");
    }
  }

  /**
   * Kết thúc đấu giá ngay lập tức (chỉ admin). TODO: Gọi AuctionDAO.endNow(auctionId) và xác định
   * winner.
   */
  private void handleEndNow() {
    winnerLabel.setText("Auction ended. The winner has been determined!");
    endnowButton.setDisable(true); // không cho end lần 2
    // TODO: AuctionDAO.endNow(currentAuctionId);
  }

  /** Đóng popup chi tiết */
  @FXML
  private void handleClose(MouseEvent e) {
    ((Stage) closeButton.getScene().getWindow()).close();
  }
}
