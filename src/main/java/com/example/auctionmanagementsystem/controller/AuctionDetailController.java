package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuctionDetailController — Popup chi tiết đấu giá (auction_detail.fxml).
 *
 * Mở từ AuctionListController.openAuctionDetail(id). Sau khi popup load xong, caller gọi
 * loadAuction(id) để điền dữ liệu.
 *
 * Tính năng: - Hiển thị thông tin đầy đủ của auction (ảnh, giá, thời gian, mô tả) - Đặt giá (Place
 * Bid) - Kết thúc đấu giá sớm (End Now — chỉ admin) - Đăng comment
 *
 * TODO: Thay dữ liệu mẫu trong loadAuction() bằng AuctionDAO.getById(id). Kết nối BidDAO.placeBid()
 * và CommentDAO.post().
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

  // ── FXML fields — comment section ────────────────────────────────────────
  @FXML
  private MFXTextField commentField;
  @FXML
  private MFXButton commentPostButton;
  @FXML
  private MFXScrollPane commentsPane; // chứa danh sách comment

  // closeButton là ImageView (không phải MFXButton) — dùng onMouseClicked
  @FXML
  private ImageView closeButton;

  /** ID auction đang xem */
  private int currentAuctionId;

  /** Danh sách comment trong session (chưa lưu DB) */
  private final List<String[]> commentList = new ArrayList<>();

  @FXML
  public void initialize() {
    // Khởi tạo trạng thái ban đầu
    bidValidationLabel.setText("");
    winnerLabel.setText("");
    endnowButton.setVisible(false); // ẩn mặc định, chỉ hiện nếu admin

    // Wire sự kiện
    placeBidButton.setOnAction(e -> handlePlaceBid());
    endnowButton.setOnAction(e -> handleEndNow());
    commentPostButton.setOnAction(e -> handlePostComment());
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

    loadComments();
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

  // ── Comment handling ──────────────────────────────────────────────────────

  /**
   * Đăng comment mới. Thêm vào list tạm và reload danh sách comment. TODO: Gọi CommentDAO.post()
   * thay vì lưu trong memory.
   */
  private void handlePostComment() {
    String text = commentField.getText().trim();
    if (text.isEmpty())
      return;

    String username = SessionManager.getInstance().getUsername();
    // Lưu tạm: [username, text, timeAgo]
    commentList.add(new String[] {username, text, "Just now"});
    commentField.setText("");
    loadComments();
  }

  /**
   * Render lại danh sách comment vào commentsPane. Load comment_card.fxml cho mỗi comment và set
   * vào VBox.
   */
  private void loadComments() {
    VBox commentBox = new VBox(8);
    for (String[] c : commentList) {
      try {
        FXMLLoader loader =
            new FXMLLoader(NavigationUtil.class.getResource("../" + NavigationUtil.COMMENT));
        Node node = loader.load();
        CommentController ctrl = loader.getController();
        ctrl.setComment(c[0], c[1], c[2]);
        commentBox.getChildren().add(node);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    commentsPane.setContent(commentBox);
  }

  /** Đóng popup chi tiết */
  @FXML
  private void handleClose(MouseEvent e) {
    ((Stage) closeButton.getScene().getWindow()).close();
  }
}
