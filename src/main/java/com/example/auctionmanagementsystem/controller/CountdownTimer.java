package com.example.auctionmanagementsystem.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;

/**
 * Đồng hồ đếm ngược dùng chung cho phần hiển thị thời gian còn lại của phiên đấu giá.
 *
 * Tự dừng Timeline khi Label bị gỡ khỏi scene (tránh rò rỉ khi đóng popup / reload list).
 * Tách ra từ AuctionDetailController để controller gọn hơn và có thể tái sử dụng.
 */
public final class CountdownTimer {

    private final Label label;
    private Timeline timeline;

    public CountdownTimer(Label label) {
        this.label = label;
        if (label != null) {
            label.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) stop();
            });
        }
    }

    /** Bắt đầu đếm ngược tới {@code endTime}. Gọi lại sẽ reset đồng hồ. */
    public void start(LocalDateTime endTime) {
        stop();
        if (endTime == null) {
            if (label != null) label.setText("---");
            return;
        }
        tick(endTime);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick(endTime)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /** Dừng đồng hồ (an toàn khi gọi nhiều lần). */
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void tick(LocalDateTime endTime) {
        java.time.Duration remaining = java.time.Duration.between(LocalDateTime.now(), endTime);
        if (remaining.isNegative() || remaining.isZero()) {
            if (label != null) label.setText("Ended");
            stop();
            return;
        }
        long days    = remaining.toDays();
        long hours   = remaining.toHours()    % 24;
        long minutes = remaining.toMinutes()  % 60;
        long seconds = remaining.getSeconds() % 60;
        String text = days > 0
                ? String.format("%dd  %02d:%02d:%02d", days, hours, minutes, seconds)
                : String.format("%02d:%02d:%02d", hours, minutes, seconds);
        if (label != null) label.setText(text);
    }
}