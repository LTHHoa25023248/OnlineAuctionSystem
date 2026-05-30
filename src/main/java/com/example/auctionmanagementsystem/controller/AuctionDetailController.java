package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AuctionStatus;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.service.ImageStorageService;

// ── THÊM IMPORT OBSERVER ──────────────────────────────────────────────────
import com.example.auctionmanagementsystem.observer.Observer;
import com.example.auctionmanagementsystem.observer.AuctionNotifier;
// ──────────────────────────────────────────────────────────────────────────

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuctionDetailController implements Observer {

  // ── FXML fields — auction info ────────────────────────────────────────────
  @FXML private ImageView image;
  @FXML private Label titleLabel;
  @FXML private Label priceLabel;
  @FXML private Label totalBidsLabel;
  @FXML private Label startLabel;
  @FXML private Label endLabel;
  @FXML private Label countdownLabel;
  @FXML private Label categoryLabel;
  @FXML private Label activeLabel;
  @FXML private Label popularNowLabel;
  @FXML private Label descriptionLabel;
  @FXML private Label winnerLabel;
  @FXML private Label paymentInfoLabel;
  @FXML private Label bidValidationLabel;

  // ── FXML fields — bid history ─────────────────────────────────────────────
  @FXML private ListView<String> bidHistoryList;
  @FXML private LineChart<String, Number> bidChart;

  // ── FXML fields — seller actions ─────────────────────────────────────────
  @FXML private HBox sellerActionsDetailBox;

  // ── FXML fields — bid section ─────────────────────────────────────────────
  @FXML private MFXTextField bidField;
  @FXML private MFXButton placeBidButton;
  @FXML private MFXButton endnowButton;

  // ── FXML fields — auto bid section ───────────────────────────────────────
  @FXML private VBox autoBidSection;
  @FXML private MFXTextField maxBidField;
  @FXML private MFXTextField incrementField;
  @FXML private MFXButton autoBidButton;
  @FXML private Label autoBidStatusLabel;

  @FXML private ImageView closeButton;

  private Auction currentAuction;
  private CountdownTimer countdown;
  // Đánh dấu user hiện tại đã từng đặt giá ở phiên này → để biết khi nào hiện dialog THUA
  private boolean currentUserHasBid = false;

  @FXML
  public void initialize() {
    bidValidationLabel.setText("");
    winnerLabel.setText("");
    endnowButton.setVisible(false);

    if (countdownLabel     != null) countdownLabel.setText("---");
    if (autoBidStatusLabel != null) autoBidStatusLabel.setText("");
    if (paymentInfoLabel   != null) paymentInfoLabel.setText("");
    if (autoBidSection     != null) { autoBidSection.setVisible(false); autoBidSection.setManaged(false); }
    if (sellerActionsDetailBox != null) { sellerActionsDetailBox.setVisible(false); sellerActionsDetailBox.setManaged(false); }

    placeBidButton.setOnAction(e -> handlePlaceBid());
    endnowButton.setOnAction(e -> handleEndNow());
    if (autoBidButton != null) autoBidButton.setOnAction(e -> handleRegisterAutoBid());
    closeButton.setOnMouseClicked(this::handleClose);

    // ── ĐĂNG KÝ VÀO HỆ THỐNG TRUYỀN TIN RAM ───────────────────────────────────
    AuctionNotifier.getInstance().registerObserver(this);

    // Cơ chế an toàn: Tự hủy đăng ký lắng nghe nếu người dùng tắt popup bằng nút [X] của hệ điều hành
    bidValidationLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
      if (newScene != null) {
        newScene.windowProperty().addListener((wObs, oldWindow, newWindow) -> {
          if (newWindow != null) {
            ((Stage) newWindow).setOnCloseRequest(windowEvent -> {
              AuctionNotifier.getInstance().removeObserver(this);
            });
          }
        });
      }
    });
  }

  // ── TRIỂN KHAI PHƯƠNG THỨC UPDATE ĐỂ ĐỔI GIAO DIỆN REALTIME ────────────────
  @Override
  public void update(String message) {
    if (message == null || currentAuction == null) return;

    String[] parts = message.split("\\|");
    if (parts.length < 2) return;

    String eventType = parts[0];

    // Xử lý sự kiện nhận lượt bid mới
    if ("NEW_BID".equals(eventType) && parts.length >= 4) {
      try {
        int targetAuctionId = Integer.parseInt(parts[1]);

        // Chỉ thay đổi giao diện nếu ID phiên nhận được trùng với phiên đang mở trên màn hình
        if (currentAuction.getId() == targetAuctionId) {
          double newPrice = Double.parseDouble(parts[2]);
          String bidderName = parts[3];

          // Chạy trong Platform.runLater để JavaFX cập nhật giao diện không bị crash thread
          Platform.runLater(() -> {
            // 1. Cập nhật giá tiền trong RAM thực thể và nhãn hiển thị
            currentAuction.setCurrentPrice(newPrice);
            priceLabel.setText(String.format("%,.2f USD", newPrice));

            String timeStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

            // 2. Chèn dòng lịch sử realtime mới lên đầu ListView
            if (bidHistoryList != null) {
              String historyEntry = String.format("Realtime  |  %,.2f USD  |  %s (%s)", newPrice, timeStr, bidderName);
              bidHistoryList.getItems().add(0, historyEntry);
            }

            // 3. Thêm điểm mới vào line chart realtime
            if (bidChart != null) {
              if (bidChart.getData().isEmpty()) {
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName("Bid Price (USD)");
                bidChart.getData().add(s);
              }
              bidChart.getData().get(0).getData().add(new XYChart.Data<>(timeStr, newPrice));
              bidChart.setVisible(true);
              bidChart.setManaged(true);
              applyChartStyle();
            }

            // 4. Tự tăng số lượng tổng lượt bids lên giao diện
            try {
              int currentBidsCount = Integer.parseInt(totalBidsLabel.getText().replaceAll("[^0-9]", ""));
              totalBidsLabel.setText((currentBidsCount + 1) + " bids");
            } catch (Exception e) {
              totalBidsLabel.setText("Updated bids");
            }

            // 5. Thông báo dải băng trạng thái màu xanh lá
            bidValidationLabel.setStyle("-fx-text-fill: #3DBA7F;");
            bidValidationLabel.setText("New bid placed by " + bidderName + "!");
          });
        }
      } catch (Exception ex) {
        System.err.println("[RealtimeUpdate] Error parsing price details: " + ex.getMessage());
      }
    }
    // Xu ly su kien anti-sniping gia han thoi gian
    else if ("TIME_EXTENDED".equals(eventType) && parts.length >= 3) {
      try {
        int targetAuctionId = Integer.parseInt(parts[1]);
        if (currentAuction.getId() == targetAuctionId) {
          LocalDateTime newEndTime = LocalDateTime.parse(parts[2]);
          Platform.runLater(() -> {
            currentAuction.setEndTime(newEndTime);
            startCountdown(newEndTime, currentAuction.getStatus());
            bidValidationLabel.setStyle("-fx-text-fill: #E8A838;");
            bidValidationLabel.setText("Auction time extended by 60 seconds!");
          });
        }
      } catch (Exception ex) {
        System.err.println("[RealtimeUpdate] Error parsing time extension: " + ex.getMessage());
      }
    }
    // Xử lý sự kiện thay đổi trạng thái hệ thống ngầm
    else if ("STATUS_CHANGE".equals(eventType) && parts.length >= 3) {
      String affectedItemName = parts[1];
      String newStatusValue = parts[2];

      if (currentAuction.getItem() != null && affectedItemName.equals(currentAuction.getItem().getName())) {
        Platform.runLater(() -> {
          bidValidationLabel.setStyle("-fx-text-fill: #E8A838;");
          bidValidationLabel.setText("Notice: Status changed to " + newStatusValue);
          loadAuction(currentAuction.getId());
        });
      }
    }
    // Xử lý sự kiện kết thúc phiên đấu giá và công bố người thắng
    else if ("AUCTION_RESULT".equals(eventType) && parts.length >= 7) {
      try {
        int targetAuctionId = Integer.parseInt(parts[1]);
        if (currentAuction.getId() == targetAuctionId) {
          int winnerId    = Integer.parseInt(parts[3]);
          double finalPrice    = Double.parseDouble(parts[4]);
          double bidderBalance = Double.parseDouble(parts[5]);
          double sellerBalance = Double.parseDouble(parts[6]);
          String itemName = parts[2];

          Platform.runLater(() -> {
            // Dừng đồng hồ đếm ngược
            if (countdown != null) countdown.stop();
            if (countdownLabel != null) countdownLabel.setText("Ended");

            // Cập nhật trạng thái badge
            activeLabel.setText(statusLabel(AuctionStatus.PAID));

            // Vô hiệu hoá khu vực đặt giá
            placeBidButton.setDisable(true);
            bidField.setDisable(true);
            endnowButton.setVisible(false);
            if (autoBidSection != null) {
              autoBidSection.setVisible(false);
              autoBidSection.setManaged(false);
            }

            int currentUserId = SessionManager.getInstance().getUserId();

            // Hiển thị người thắng cuộc nổi bật
            if (winnerId > 0) {
              boolean isCurrentUserWinner = (currentUserId == winnerId);
              String winnerText = isCurrentUserWinner
                      ? "WINNER: Bidder #" + winnerId + "  ★ YOU WON! ★"
                      : "WINNER: Bidder #" + winnerId;
              winnerLabel.setText(winnerText);
              winnerLabel.setStyle(isCurrentUserWinner
                      ? "-fx-text-fill: #27AE60; -fx-font-size: 15px; -fx-font-weight: bold;"
                      : "-fx-text-fill: #F4A622; -fx-font-size: 15px; -fx-font-weight: bold;");
            } else {
              winnerLabel.setText("Auction ended — No bids placed");
              winnerLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;");
            }

            bidValidationLabel.setStyle("-fx-text-fill: #E8A838;");
            bidValidationLabel.setText(String.format(
                    "Auction has ended. Final price: %,.2f USD", finalPrice));

            // Người thắng cuộc: hiển thị dialog chúc mừng + số dư còn lại
            if (winnerId > 0 && currentUserId == winnerId && bidderBalance >= 0) {
              showWinnerDialog(itemName, finalPrice, bidderBalance);
            }
            // Seller: hiển thị dialog thông báo tiền đã được cộng
            else if (currentAuction.getSeller() != null
                    && currentAuction.getSeller().getId() == currentUserId
                    && winnerId > 0 && sellerBalance >= 0) {
              showSellerDialog(itemName, finalPrice, sellerBalance);
            }
            // Người đã từng đặt giá nhưng KHÔNG thắng: hiển thị dialog THUA
            else if (winnerId > 0 && currentUserId != winnerId && currentUserHasBid
                    && (currentAuction.getSeller() == null
                    || currentAuction.getSeller().getId() != currentUserId)) {
              showLoserDialog(itemName, finalPrice, "Bidder #" + winnerId);
            }
          });
        }
      } catch (Exception ex) {
        System.err.println("[RealtimeUpdate] Error parsing auction result: " + ex.getMessage());
      }
    }
  }
  // ──────────────────────────────────────────────────────────────────────────

  public void loadAuction(int auctionId) {
    bidValidationLabel.setText("Loading...");

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        JsonObject data = ApiClient.getObject("/auction/detail?id=" + auctionId);
        if (data.has("error")) {
          Platform.runLater(() -> bidValidationLabel.setText("Auction not found."));
          return null;
        }
        AuctionDetailMapper.Result r = AuctionDetailMapper.parse(data);
        currentAuction = r.auction;
        Platform.runLater(() -> fillUI(r.item, r.bids));
        return null;
      }
    };

    task.setOnFailed(e -> Platform.runLater(() ->
            bidValidationLabel.setText("Load error: " + task.getException().getMessage())));

    new Thread(task).start();
  }

  private void fillUI(Item item, List<BidTransaction> bids) {
    int bidCount = bids.size();
    bidValidationLabel.setText("");

    titleLabel.setText(item != null ? item.getName() : "Unknown Item");
    priceLabel.setText(String.format("%,.2f USD", currentAuction.getCurrentPrice()));
    totalBidsLabel.setText(bidCount + " bids");

    if (bidHistoryList != null) {
      javafx.collections.ObservableList<String> entries =
              javafx.collections.FXCollections.observableArrayList();
      for (int i = bids.size() - 1; i >= 0; i--) {
        BidTransaction b = bids.get(i);
        entries.add(String.format("#%d  |  %,.2f USD  |  %s",
                b.getId(), b.getAmount(), b.getFormattedTime()));
      }
      bidHistoryList.setItems(entries);
      if (entries.isEmpty()) {
        bidHistoryList.setPlaceholder(new Label("No bids yet."));
      }
    }

    startLabel.setText(currentAuction.getStartTime() != null
            ? currentAuction.getStartTime().toLocalDate().toString() : "---");

    LocalDateTime endTime = currentAuction.getEndTime();
    if (endTime != null) {
      endLabel.setText(String.format("%s  %02d:%02d:%02d",
              endTime.toLocalDate(),
              endTime.getHour(), endTime.getMinute(), endTime.getSecond()));
    } else {
      endLabel.setText("---");
    }

    String itemType = item != null ? item.getClass().getSimpleName().toUpperCase() : "";
    categoryLabel.setText(switch (itemType) {
      case "VEHICLE" -> "Vehicle";
      case "ART"     -> "Art";
      default        -> "Electronics";
    });

    AuctionStatus status = currentAuction.getStatus();
    activeLabel.setText(statusLabel(status));

    popularNowLabel.setVisible(bidCount > 10);
    popularNowLabel.setManaged(bidCount > 10);

    descriptionLabel.setText(item != null && item.getDescription() != null
            ? item.getDescription() : "");

    if (paymentInfoLabel != null) paymentInfoLabel.setText("");

    if ((status == AuctionStatus.FINISHED || status == AuctionStatus.PAID)
            && currentAuction.getHighestBidder() != null) {
      int winnerId = currentAuction.getHighestBidder().getId();
      int currentUserId = SessionManager.getInstance().getUserId();
      boolean isCurrentUserWinner = (currentUserId == winnerId);
      winnerLabel.setText(isCurrentUserWinner
              ? "WINNER: Bidder #" + winnerId + "  ★ YOU WON! ★"
              : "WINNER: Bidder #" + winnerId);
      winnerLabel.setStyle(isCurrentUserWinner
              ? "-fx-text-fill: #27AE60; -fx-font-size: 15px; -fx-font-weight: bold;"
              : "-fx-text-fill: #F4A622; -fx-font-size: 15px; -fx-font-weight: bold;");
      if (status == AuctionStatus.PAID) {
        boolean isSellerViewing = currentAuction.getSeller() != null
                && currentAuction.getSeller().getId() == currentUserId;
        if (isCurrentUserWinner || isSellerViewing) {
          fetchAndShowBalance(currentUserId, isCurrentUserWinner);
        }
      }
    } else if (status == AuctionStatus.FINISHED || status == AuctionStatus.PAID) {
      winnerLabel.setText("Auction ended — No bids placed");
      winnerLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;");
    } else {
      winnerLabel.setText("");
      winnerLabel.setStyle("");
    }

    startCountdown(endTime, status);

    boolean canBid = (status == AuctionStatus.RUNNING || status == AuctionStatus.OPEN);
    placeBidButton.setDisable(!canBid);
    bidField.setDisable(!canBid);
    if (!canBid) {
      bidValidationLabel.setStyle("-fx-text-fill: #E8A838;");
      String statusMsg = switch (status) {
        case PENDING  -> "Auction is pending admin approval.";
        case FINISHED -> "Auction has already ended.";
        case REJECTED -> {
          String reason = currentAuction.getRejectReason();
          yield "Auction was rejected by admin."
                  + (reason != null && !reason.isBlank() ? "\nReason: " + reason : "");
        }
        case CANCELED -> "Auction has been cancelled.";
        default       -> "Auction is not active.";
      };
      bidValidationLabel.setText(statusMsg);
    }

    boolean isBidder = !SessionManager.getInstance().isAdmin()
            && !SessionManager.getInstance().isSeller();
    if (autoBidSection != null) {
      boolean show = canBid && isBidder;
      autoBidSection.setVisible(show);
      autoBidSection.setManaged(show);
    }

    boolean canEnd = SessionManager.getInstance().isAdmin() && status == AuctionStatus.RUNNING;
    endnowButton.setVisible(canEnd);
    endnowButton.setDisable(!canEnd);

    boolean isSeller = currentAuction.getSeller() != null
            && currentAuction.getSeller().getId() == SessionManager.getInstance().getUserId();
    boolean canEdit = isSeller && (status == AuctionStatus.PENDING || status == AuctionStatus.OPEN);
    if (sellerActionsDetailBox != null) {
      sellerActionsDetailBox.setVisible(canEdit);
      sellerActionsDetailBox.setManaged(canEdit);
    }

    if (image != null && item != null && item.getImagePath() != null) {
      try {
        File file = new File(ImageStorageService.getFullPath(item.getImagePath()));
        if (file.exists()) {
          image.setImage(new Image(file.toURI().toString()));
          image.setPreserveRatio(true);
        }
      } catch (Exception ignored) {}
    }

    fillBidChart(bids);
  }

  private void fillBidChart(List<BidTransaction> bids) {
    if (bidChart == null) return;
    bidChart.getData().clear();
    if (bids.isEmpty()) {
      bidChart.setVisible(false);
      bidChart.setManaged(false);
      return;
    }
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Bid Price (USD)");
    for (BidTransaction b : bids) {
      String label = b.getTime() != null
          ? b.getTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
          : "#" + b.getId();
      series.getData().add(new XYChart.Data<>(label, b.getAmount()));
    }
    bidChart.getData().add(series);
    bidChart.setVisible(true);
    bidChart.setManaged(true);
    applyChartStyle();
  }

  // Programmatic styling — CSS alone cannot override JavaFX chart's internal dark layers
  private void applyChartStyle() {
    Platform.runLater(() -> {
      bidChart.setStyle("-fx-background-color: white;");
      // Force white on chart-content (axis label area)
      javafx.scene.Node content = bidChart.lookup(".chart-content");
      if (content != null) content.setStyle("-fx-background-color: white;");
      // Force white on inner plot area
      javafx.scene.Node plot = bidChart.lookup(".chart-plot-background");
      if (plot != null) plot.setStyle("-fx-background-color: white;");
      // Thin dark line
      javafx.scene.Node line = bidChart.lookup(".default-color0.chart-series-line");
      if (line != null) line.setStyle("-fx-stroke: #1A1A1A; -fx-stroke-width: 1.8px;");
      // Small solid dark dots
      for (javafx.scene.Node sym : bidChart.lookupAll(".default-color0.chart-line-symbol")) {
        sym.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 4px; -fx-background-radius: 5px;");
      }
    });
  }

  private void startCountdown(LocalDateTime endTime, AuctionStatus status) {
    if (countdown == null) countdown = new CountdownTimer(countdownLabel);
    if (endTime == null || (status != AuctionStatus.RUNNING && status != AuctionStatus.OPEN)) {
      countdown.stop();
      if (countdownLabel != null) countdownLabel.setText("---");
      return;
    }
    countdown.start(endTime);
  }

  @FXML
  private void handleDetailEdit() {
    if (currentAuction == null) return;
    Item item = currentAuction.getItem();
    if (item == null) return;
    showEditDialogs(item);
  }

  private void showEditDialogs(Item item) {
    if (item == null) return;

    TextInputDialog nameDialog = new TextInputDialog(item.getName());
    nameDialog.setTitle("Edit Item");
    nameDialog.setHeaderText("Update item name");
    nameDialog.setContentText("Name:");
    Optional<String> newName = nameDialog.showAndWait();
    if (newName.isEmpty() || newName.get().isBlank()) return;

    TextInputDialog descDialog = new TextInputDialog(
            item.getDescription() != null ? item.getDescription() : "");
    descDialog.setTitle("Edit Item");
    descDialog.setHeaderText("Update description");
    descDialog.setContentText("Description:");
    Optional<String> newDesc = descDialog.showAndWait();
    if (newDesc.isEmpty()) return;

    item.setName(newName.get().trim());
    item.setDescription(newDesc.get().trim());

    Task<Void> save = new Task<>() {
      @Override protected Void call() throws Exception {
        JsonObject resp = ApiClient.post("/item/update", Map.of(
                "itemId", item.getId(), "name", item.getName(), "description", item.getDescription()));
        if (!resp.get("success").getAsBoolean())
          throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "Update failed");
        return null;
      }
    };
    save.setOnSucceeded(e -> Platform.runLater(() -> {
      titleLabel.setText(item.getName());
      descriptionLabel.setText(item.getDescription());
      bidValidationLabel.setStyle("-fx-text-fill: #3DBA7F;");
      bidValidationLabel.setText("Item updated successfully.");
    }));
    save.setOnFailed(e -> Platform.runLater(() -> {
      bidValidationLabel.setStyle("-fx-text-fill: #E05454;");
      bidValidationLabel.setText(save.getException().getMessage());
    }));
    new Thread(save).start();
  }

  @FXML
  private void handleDetailDelete() {
    if (currentAuction == null) return;
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete this auction and its item permanently?", ButtonType.YES, ButtonType.NO);
    confirm.setTitle("Confirm Delete");
    confirm.setHeaderText("Auction ID: " + currentAuction.getId());
    confirm.showAndWait().ifPresent(btn -> {
      if (btn != ButtonType.YES) return;
      int auctionId = currentAuction.getId();
      int itemId    = currentAuction.getItem().getId();
      Task<Void> task = new Task<>() {
        @Override protected Void call() throws Exception {
          JsonObject resp = ApiClient.post("/item/delete", Map.of("auctionId", auctionId, "itemId", itemId));
          if (!resp.get("success").getAsBoolean())
            throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "Delete failed");
          return null;
        }
      };
      task.setOnSucceeded(e -> Platform.runLater(() -> {
        // HỦY ĐĂNG KÝ KHI PHIÊN BỊ Seller XÓA
        AuctionNotifier.getInstance().removeObserver(this);
        ((Stage) closeButton.getScene().getWindow()).close();
      }));
      task.setOnFailed(e -> {
        Throwable cause = task.getException().getCause();
        String msg = cause != null ? cause.getMessage() : task.getException().getMessage();
        Platform.runLater(() -> {
          bidValidationLabel.setStyle("-fx-text-fill: #E05454;");
          bidValidationLabel.setText("Delete failed: " + msg);
        });
      });
      new Thread(task).start();
    });
  }

  private void handlePlaceBid() {
    bidValidationLabel.setStyle("-fx-text-fill: #E05454;");
    bidValidationLabel.setText("");
    if (currentAuction == null) { bidValidationLabel.setText("Auction not loaded yet."); return; }

    String raw = bidField.getText().trim();
    if (raw.isEmpty()) { bidValidationLabel.setText("Please enter a bid amount."); return; }
    double bid;
    try {
      bid = Double.parseDouble(raw);
      if (bid <= 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
      bidValidationLabel.setText("Invalid amount.");
      return;
    }

    final double finalBid = bid;
    placeBidButton.setDisable(true);
    placeBidButton.setText("Placing...");

    Task<Double> task = new Task<>() {
      @Override protected Double call() throws Exception {
        JsonObject resp = ApiClient.post("/auction/bid", Map.of(
                "auctionId", currentAuction.getId(),
                "bidderId",  SessionManager.getInstance().getUserId(),
                "amount",    finalBid));
        if (!resp.get("success").getAsBoolean())
          throw new RuntimeException(resp.get("error").getAsString());
        return resp.get("newPrice").getAsDouble();
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      double newPrice = task.getValue();
      currentUserHasBid = true;
      currentAuction.setCurrentPrice(newPrice);
      bidValidationLabel.setStyle("-fx-text-fill: #3DBA7F;");
      bidValidationLabel.setText(String.format("Bid of %,.2f USD placed successfully!", finalBid));
      priceLabel.setText(String.format("%,.2f USD", newPrice));
      bidField.setText("");
      placeBidButton.setDisable(false);
      placeBidButton.setText("Place Bid");
    }));
    task.setOnFailed(e -> Platform.runLater(() -> {
      Throwable cause = task.getException().getCause();
      bidValidationLabel.setText(cause != null ? cause.getMessage() : task.getException().getMessage());
      placeBidButton.setDisable(false);
      placeBidButton.setText("Place Bid");
    }));
    new Thread(task).start();
  }

  private void handleEndNow() {
    if (currentAuction == null) return;
    endnowButton.setDisable(true);

    Task<JsonObject> task = new Task<>() {
      @Override protected JsonObject call() throws Exception {
        JsonObject resp = ApiClient.post("/auction/end", Map.of("auctionId", currentAuction.getId()));
        if (!resp.get("success").getAsBoolean())
          throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "End failed");
        return resp;
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      JsonObject resp = task.getValue();
      Integer winnerId = (resp.has("winnerId") && !resp.get("winnerId").isJsonNull())
              ? resp.get("winnerId").getAsInt() : null;
      double finalPrice = resp.has("finalPrice")
              ? resp.get("finalPrice").getAsDouble() : currentAuction.getCurrentPrice();
      Double bidderNewBalance = (resp.has("bidderNewBalance") && !resp.get("bidderNewBalance").isJsonNull())
              ? resp.get("bidderNewBalance").getAsDouble() : null;
      Double sellerNewBalance = (resp.has("sellerNewBalance") && !resp.get("sellerNewBalance").isJsonNull())
              ? resp.get("sellerNewBalance").getAsDouble() : null;

      activeLabel.setText(statusLabel(AuctionStatus.PAID));
      if (countdown != null) countdown.stop();
      if (countdownLabel != null) countdownLabel.setText("Ended");
      placeBidButton.setDisable(true);
      bidField.setDisable(true);
      endnowButton.setVisible(false);
      if (autoBidSection != null) { autoBidSection.setVisible(false); autoBidSection.setManaged(false); }

      if (winnerId != null) {
        winnerLabel.setText("WINNER: Bidder #" + winnerId);
        winnerLabel.setStyle("-fx-text-fill: #F4A622; -fx-font-size: 15px; -fx-font-weight: bold;");
        bidValidationLabel.setStyle("-fx-text-fill: #3DBA7F;");
        bidValidationLabel.setText(String.format(
                "Auction ended! Winner: Bidder #%d | Price: $%,.2f | Automatic payment completed.",
                winnerId, finalPrice));
        // Hiển thị số dư mới trong paymentInfoLabel (Admin xem)
        if (paymentInfoLabel != null && bidderNewBalance != null && sellerNewBalance != null) {
          paymentInfoLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
          paymentInfoLabel.setText(String.format(
                  "Bidder #%d's balance after payment: $%,.2f  |  Seller's balance after receiving payment: $%,.2f",
                  winnerId, bidderNewBalance, sellerNewBalance));
        }
      } else {
        winnerLabel.setText("Auction ended — No bids placed");
        winnerLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px;");
        bidValidationLabel.setStyle("-fx-text-fill: #E8A838;");
        bidValidationLabel.setText("Auction ended. No payment made.");
      }
    }));
    task.setOnFailed(e -> Platform.runLater(() -> {
      Throwable cause = task.getException().getCause();
      bidValidationLabel.setStyle("-fx-text-fill: #E05454;");
      bidValidationLabel.setText(cause != null ? cause.getMessage() : task.getException().getMessage());
      endnowButton.setDisable(false);
    }));
    new Thread(task).start();
  }

  private void handleRegisterAutoBid() {
    if (autoBidStatusLabel != null) { autoBidStatusLabel.setStyle("-fx-text-fill: #E05454;"); autoBidStatusLabel.setText(""); }
    if (currentAuction == null) return;

    String rawMax = maxBidField   != null ? maxBidField.getText().trim()   : "";
    String rawInc = incrementField!= null ? incrementField.getText().trim() : "";
    if (rawMax.isEmpty() || rawInc.isEmpty()) {
      if (autoBidStatusLabel != null) autoBidStatusLabel.setText("Please fill in max bid and increment.");
      return;
    }
    double maxBid, increment;
    try {
      maxBid    = Double.parseDouble(rawMax);
      increment = Double.parseDouble(rawInc);
      if (maxBid <= currentAuction.getCurrentPrice() || increment <= 0) throw new NumberFormatException();
    } catch (NumberFormatException ex) {
      if (autoBidStatusLabel != null)
        autoBidStatusLabel.setText("Max bid must exceed current price. Increment must be > 0.");
      return;
    }

    if (autoBidButton != null) { autoBidButton.setDisable(true); autoBidButton.setText("Registering..."); }
    final double fMax = maxBid, fInc = increment;

    Task<Void> task = new Task<>() {
      @Override protected Void call() throws Exception {
        JsonObject resp = ApiClient.post("/auction/autobid", Map.of(
                "auctionId", currentAuction.getId(),
                "bidderId",  SessionManager.getInstance().getUserId(),
                "maxBid",    fMax,
                "increment", fInc));
        if (!resp.get("success").getAsBoolean())
          throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "Auto bid failed");
        return null;
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      if (autoBidStatusLabel != null) {
        autoBidStatusLabel.setStyle("-fx-text-fill: #3DBA7F;");
        autoBidStatusLabel.setText(String.format("Auto bid registered: max %,.2f USD, +%,.2f USD/bid.", fMax, fInc));
      }
      if (maxBidField   != null) maxBidField.setText("");
      if (incrementField!= null) incrementField.setText("");
      if (autoBidButton != null) { autoBidButton.setDisable(false); autoBidButton.setText("Register Auto Bid"); }
    }));
    task.setOnFailed(e -> Platform.runLater(() -> {
      Throwable cause = task.getException().getCause();
      if (autoBidStatusLabel != null)
        autoBidStatusLabel.setText(cause != null ? cause.getMessage() : task.getException().getMessage());
      if (autoBidButton != null) { autoBidButton.setDisable(false); autoBidButton.setText("Register Auto Bid"); }
    }));
    new Thread(task).start();
  }

  private void showWinnerDialog(String itemName, double finalPrice, double remainingBalance) {
    Window owner = winnerLabel.getScene() != null ? winnerLabel.getScene().getWindow() : null;
    ResultDialog.showWin(owner, itemName, finalPrice, remainingBalance);
  }

  private void showLoserDialog(String itemName, double finalPrice, String winnerInfo) {
    Window owner = winnerLabel.getScene() != null ? winnerLabel.getScene().getWindow() : null;
    ResultDialog.showLose(owner, itemName, finalPrice, winnerInfo);
  }

  private void showSellerDialog(String itemName, double finalPrice, double newBalance) {
    Window owner = winnerLabel.getScene() != null ? winnerLabel.getScene().getWindow() : null;
    ResultDialog.showSeller(owner, itemName, finalPrice, newBalance);
  }

  private void fetchAndShowBalance(int userId, boolean isWinner) {
    Task<Double> task = new Task<>() {
      @Override protected Double call() throws Exception {
        JsonObject resp = ApiClient.getObject("/user/balance?userId=" + userId);
        return resp.get("balance").getAsDouble();
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      double balance = task.getValue();
      if (paymentInfoLabel != null) {
        paymentInfoLabel.setStyle(isWinner
                ? "-fx-text-fill: #27AE60; -fx-font-size: 13px; -fx-font-weight: bold;"
                : "-fx-text-fill: #1565C0; -fx-font-size: 13px; -fx-font-weight: bold;");
        paymentInfoLabel.setText(isWinner
                ? String.format("Remaining account balance after payment: $%,.2f", balance)
                : String.format("Account balance after receiving payment: $%,.2f", balance));
      }
    }));
    new Thread(task).start();
  }

  private String statusLabel(AuctionStatus status) {
    return switch (status) {
      case PENDING  -> "Pending";
      case OPEN     -> "Open";
      case RUNNING  -> "Active";
      case FINISHED -> "Finished";
      case REJECTED -> "Rejected";
      case CANCELED -> "Cancelled";
      case PAID     -> "Paid";
    };
  }

  @FXML
  private void handleClose(MouseEvent e) {
    if (countdown != null) countdown.stop();
    // ── HỦY ĐĂNG KÝ QUAN SÁT VIÊN TRÁNH RÒ RỈ RAM CHẠY NGẦM ───────────────────
    AuctionNotifier.getInstance().removeObserver(this);
    ((Stage) closeButton.getScene().getWindow()).close();
  }
}