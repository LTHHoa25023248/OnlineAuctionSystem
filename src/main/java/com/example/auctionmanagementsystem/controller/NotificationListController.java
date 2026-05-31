package com.example.auctionmanagementsystem.controller;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Controller cho màn hình danh sách thông báo (notification_list.fxml).
 *
 * Chỉ là tầng hiển thị: nó bind thẳng vào {@link NotificationStore} nên không
 * cần tự đăng ký Observer (AuctionListController đã làm việc đó và bơm dữ liệu
 * vào store). Mọi thông báo tới khi popup đang mở vẫn hiện ngay nhờ ObservableList.
 */
public class NotificationListController {

    @FXML private ListView<String> notificationList;
    @FXML private Label emptyLabel;
    @FXML private MFXButton clearButton;
    @FXML private MFXButton closeButton;

    @FXML
    public void initialize() {
        NotificationStore store = NotificationStore.getInstance();

        // Cùng nguồn dữ liệu với badge ngoài chuông
        notificationList.setItems(store.getMessages());

        // Cell tự xuống dòng cho thông báo dài
        notificationList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(true);
                }
            }
        });

        // Mở panel = coi như đã đọc hết -> tắt badge đỏ
        store.markAllRead();

        // Hiện dòng "chưa có thông báo" khi rỗng
        bindEmptyState(store);

        if (clearButton != null) clearButton.setOnAction(e -> store.clear());
        if (closeButton != null) closeButton.setOnAction(e -> close());
    }

    private void bindEmptyState(NotificationStore store) {
        Runnable refresh = () -> {
            boolean empty = store.getMessages().isEmpty();
            if (emptyLabel != null) {
                emptyLabel.setVisible(empty);
                emptyLabel.setManaged(empty);
            }
            notificationList.setVisible(!empty);
            notificationList.setManaged(!empty);
        };
        refresh.run();
        store.getMessages().addListener(
                (javafx.collections.ListChangeListener<String>) c -> refresh.run());
    }

    private void close() {
        if (closeButton != null && closeButton.getScene() != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }
}
