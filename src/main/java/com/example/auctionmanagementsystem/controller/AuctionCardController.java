package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.google.gson.JsonObject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuctionCardController {

  @FXML private ImageView image;
  @FXML private Label title;
  @FXML private Label price;
  @FXML private Label totalBids;
  @FXML private Label timeLeft;
  @FXML private Label categoryLabel;
  @FXML private Label popularNowLabel;
  @FXML private HBox sellerActionsBox;
  @FXML private VBox attributesBox; 


  private AuctionListController parentController;
  private int auctionId;
  private int itemId;
  private LocalDateTime endTime;
  private Timeline countdownTimeline;

  public void setAuction(AuctionListController.AuctionItem item, AuctionListController parent) {
    this.auctionId        = item.id;
    this.itemId           = item.itemId;
    this.endTime          = item.endTime;
    this.parentController = parent;

    title.setText(item.name);
    price.setText(String.format("%,.0f USD", item.price));
    totalBids.setText(item.bids + " bids");
    categoryLabel.setText(item.category);

    setImage(item.imagePath);

    if (attributesBox != null) {
        attributesBox.getChildren().clear();

        Map<String, String> attrs = item.getAttributes();

        if (attrs != null && !attrs.isEmpty()) {
            for (Map.Entry<String, String> entry : attrs.entrySet()) {
              if (entry.getKey().equals("dummy")) continue;
                String keyName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                Label lblAttr = new Label("• " + keyName + ": " + entry.getValue());
                lblAttr.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #666666; -fx-font-style: italic;");
                attributesBox.getChildren().add(lblAttr);
            }
          }
    }

    boolean isPopular = item.bids > 10;
    popularNowLabel.setText("Popular Now");
    popularNowLabel.setVisible(isPopular);
    popularNowLabel.setManaged(isPopular);

    boolean isMine = item.sellerId == SessionManager.getInstance().getUserId();
    if (sellerActionsBox != null) {
      sellerActionsBox.setVisible(isMine);
      sellerActionsBox.setManaged(isMine);
    }

    startCountdown();

    title.setText(item.name);
    
    // --- ĐẶT TRẠM KIỂM SOÁT DỮ LIỆU ---
    System.out.println("🔍 [TEST CARD] Tên món đồ: " + item.name);
    System.out.println("🔍 [TEST CARD] VBox UI có bị null không? " + (attributesBox == null));
    System.out.println("🔍 [TEST CARD] Dữ liệu Attributes: " + item.getAttributes());
  }

  private void startCountdown() {
    updateCountdownLabel(); // hiện ngay lập tức, không chờ 1s
    countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdownLabel()));
    countdownTimeline.setCycleCount(Animation.INDEFINITE);
    countdownTimeline.play();
    // Dừng khi card bị remove khỏi scene (list reload)
    timeLeft.sceneProperty().addListener((obs, oldScene, newScene) -> {
      if (newScene == null && countdownTimeline != null) countdownTimeline.stop();
    });
  }

  private void updateCountdownLabel() {
    if (endTime == null) { timeLeft.setText("No end time"); return; }
    java.time.Duration remaining = java.time.Duration.between(LocalDateTime.now(), endTime);
    if (remaining.isNegative() || remaining.isZero()) {
      timeLeft.setText("Ended");
      if (countdownTimeline != null) countdownTimeline.stop();
      return;
    }
    long days    = remaining.toDays();
    long hours   = remaining.toHours()   % 24;
    long minutes = remaining.toMinutes() % 60;
    long seconds = remaining.getSeconds() % 60;
    if (days > 0) {
      timeLeft.setText(String.format("Ends in: %dd %02d:%02d:%02d", days, hours, minutes, seconds));
    } else {
      timeLeft.setText(String.format("Ends in: %02d:%02d:%02d", hours, minutes, seconds));
    }
  }

  public void setImage(String imagePath) {
    if (imagePath == null || imagePath.isBlank()) return;
    try {
      File file = new File(imagePath);
      if (file.exists()) {
        Image img = new Image(file.toURI().toString());
        image.setImage(img);
        image.setPreserveRatio(true);
      }
    } catch (Exception ignored) {}
  }

  @FXML
  private void onCardClick(MouseEvent event) {
    if (parentController != null)
      parentController.openAuctionDetail(auctionId);
  }

  @FXML
  private void onSellerActionsClick(MouseEvent event) {
    event.consume(); // ngăn click nổi lên root HBox → không mở auction detail
  }

  @FXML
  private void onEditClick() {
    Task<JsonObject> loadTask = new Task<>() {
      @Override
      protected JsonObject call() throws Exception {
        return ApiClient.getObject("/auction/detail?id=" + auctionId);
      }
    };
    loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
      JsonObject data = loadTask.getValue();
      String name = data.get("itemName").isJsonNull() ? "" : data.get("itemName").getAsString();
      String desc = data.get("itemDescription").isJsonNull() ? "" : data.get("itemDescription").getAsString();
      showEditDialogs(name, desc);
    }));
    loadTask.setOnFailed(e -> Platform.runLater(() -> showError(loadTask.getException().getMessage())));
    new Thread(loadTask).start();
  }

  private void showEditDialogs(String currentName, String currentDesc) {
    TextInputDialog nameDialog = new TextInputDialog(currentName);
    nameDialog.setTitle("Edit Item");
    nameDialog.setHeaderText("Update item name");
    nameDialog.setContentText("Name:");
    Optional<String> newName = nameDialog.showAndWait();
    if (newName.isEmpty() || newName.get().isBlank()) return;

    TextInputDialog descDialog = new TextInputDialog(currentDesc);
    descDialog.setTitle("Edit Item");
    descDialog.setHeaderText("Update item description");
    descDialog.setContentText("Description:");
    Optional<String> newDesc = descDialog.showAndWait();
    if (newDesc.isEmpty()) return;

    final String finalName = newName.get().trim();
    final String finalDesc = newDesc.get().trim();

    Task<Void> saveTask = new Task<>() {
      @Override
      protected Void call() throws Exception {
        JsonObject resp = ApiClient.post("/item/update", Map.of(
            "itemId", itemId, "name", finalName, "description", finalDesc));
        if (!resp.get("success").getAsBoolean())
          throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "Update failed");
        return null;
      }
    };
    saveTask.setOnSucceeded(ev -> Platform.runLater(() -> {
      title.setText(finalName);
      parentController.refresh();
    }));
    saveTask.setOnFailed(ev -> Platform.runLater(() -> showError(saveTask.getException().getMessage())));
    new Thread(saveTask).start();
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message != null ? message : "An unexpected error occurred.");
    alert.showAndWait();
  }

  @FXML
  private void onDeleteClick() {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Delete this auction and its item permanently?", ButtonType.YES, ButtonType.NO);
    confirm.setTitle("Confirm Delete");
    confirm.setHeaderText("Auction ID: " + auctionId);
    confirm.showAndWait().ifPresent(btn -> {
      if (btn != ButtonType.YES) return;
      Task<Void> task = new Task<>() {
        @Override
        protected Void call() throws Exception {
          JsonObject resp = ApiClient.post("/item/delete", Map.of("auctionId", auctionId, "itemId", itemId));
          if (!resp.get("success").getAsBoolean())
            throw new RuntimeException(resp.has("error") ? resp.get("error").getAsString() : "Delete failed");
          return null;
        }
      };
      task.setOnSucceeded(e -> Platform.runLater(() -> parentController.refresh()));
      task.setOnFailed(e -> Platform.runLater(() -> showError(task.getException().getMessage())));
      new Thread(task).start();
    });
  }
}
