package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CommentController {

    @FXML private Label name;
    @FXML private Label message;
    @FXML private Label time;

    public void setComment(String username, String text, String timeAgo) {
        name.setText(username);
        message.setText(text);
        time.setText(timeAgo);
    }
}