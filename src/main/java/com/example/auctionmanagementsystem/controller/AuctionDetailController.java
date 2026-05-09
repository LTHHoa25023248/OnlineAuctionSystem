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

public class AuctionDetailController {

    @FXML private ImageView     image;
    @FXML private Label         titleLabel;
    @FXML private Label         priceLabel;
    @FXML private Label         totalBidsLabel;
    @FXML private Label         startLabel;
    @FXML private Label         endLabel;
    @FXML private Label         categoryLabel;
    @FXML private Label         activeLabel;
    @FXML private Label         popularNowLabel;
    @FXML private Label         userLabel;
    @FXML private Label         descriptionLabel;
    @FXML private Label         winnerLabel;
    @FXML private Label         bidValidationLabel;

    @FXML private MFXTextField  bidField;
    @FXML private MFXButton     placeBidButton;
    @FXML private MFXButton     endnowButton;

    @FXML private MFXTextField  commentField;
    @FXML private MFXButton     commentPostButton;
    @FXML private MFXScrollPane commentsPane;

    @FXML private ImageView     closeButton;

    private int currentAuctionId;
    private List<String[]> commentList = new ArrayList<>();

    @FXML
    public void initialize() {
        bidValidationLabel.setText("");
        winnerLabel.setText("");
        endnowButton.setVisible(false);

        placeBidButton.setOnAction(e    -> handlePlaceBid());
        endnowButton.setOnAction(e      -> handleEndNow());
        commentPostButton.setOnAction(e -> handlePostComment());
        closeButton.setOnMouseClicked(this::handleClose);
    }

    public void loadAuction(int auctionId) {
        this.currentAuctionId = auctionId;

        // Demo — thay bằng DAO thực sau
        titleLabel.setText("Demo Auction #" + auctionId);
        priceLabel.setText("250.00 $");
        totalBidsLabel.setText("7");
        startLabel.setText("01/05/2025");
        endLabel.setText("31/05/2025");
        categoryLabel.setText("Jewelry");
        userLabel.setText(SessionManager.getInstance().getUsername());
        descriptionLabel.setText("Đây là mô tả sản phẩm demo.");
        endnowButton.setVisible(SessionManager.getInstance().isAdmin());

        loadComments();
    }

    private void handlePlaceBid() {
        bidValidationLabel.setText("");
        String raw = bidField.getText().trim();
        if (raw.isEmpty()) {
            bidValidationLabel.setText("Vui lòng nhập số tiền đấu giá.");
            return;
        }
        try {
            double bid = Double.parseDouble(raw);
            if (bid <= 0) throw new NumberFormatException();
            bidValidationLabel.setText("Đã đặt giá " + bid + " $!");
            bidField.setText("");
        } catch (NumberFormatException ex) {
            bidValidationLabel.setText("Số tiền không hợp lệ.");
        }
    }

    private void handleEndNow() {
        winnerLabel.setText("Đấu giá đã kết thúc. Người thắng đã được xác định!");
        endnowButton.setDisable(true);
    }

    private void handlePostComment() {
        String text = commentField.getText().trim();
        if (text.isEmpty()) return;

        // Thêm comment vào list — thay bằng CommentDAO.post() sau
        String username = SessionManager.getInstance().getUsername();
        commentList.add(new String[]{username, text, "Just now"});

        commentField.setText("");
        loadComments();
    }

    private void loadComments() {
        VBox commentBox = new VBox(8);

        for (String[] c : commentList) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        NavigationUtil.class.getResource("../" + NavigationUtil.COMMENT));
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

    @FXML
    private void handleClose(MouseEvent e) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}