package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * CommentController — Điều khiển một comment card (comment.fxml).
 *
 * Load động bởi AuctionDetailController.loadComments(). Mỗi comment trong commentsPane là một
 * instance riêng biệt.
 *
 * Chỉ có một method public: setComment() để điền dữ liệu từ ngoài vào.
 */
public class CommentController {

  // ── FXML fields — phải khớp fx:id trong comment.fxml ─────────────────────
  @FXML
  private Label name; // tên người đăng
  @FXML
  private Label message; // nội dung comment
  @FXML
  private Label time; // thời gian (vd: "Just now", "2 hours ago")

  /**
   * Điền dữ liệu vào comment card. Được gọi bởi AuctionDetailController sau khi load FXML.
   *
   * @param username Tên người đăng comment
   * @param text Nội dung comment
   * @param timeAgo Thời gian đăng (chuỗi tương đối như "Just now")
   */
  public void setComment(String username, String text, String timeAgo) {
    name.setText(username);
    message.setText(text);
    time.setText(timeAgo);
  }
}
