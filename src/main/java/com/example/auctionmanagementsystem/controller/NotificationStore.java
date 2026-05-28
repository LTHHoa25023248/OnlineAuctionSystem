package com.example.auctionmanagementsystem.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Kho thông báo dùng chung (singleton).
 *
 * Cả {@link AuctionListController} (đẩy thông báo realtime từ Observer vào đây)
 * lẫn {@code NotificationListController} (hiển thị) đều đọc/ghi trên cùng một
 * danh sách, nên badge ở chuông và ListView ở popup luôn đồng bộ.
 *
 * Lưu ý: state này tồn tại theo vòng đời ứng dụng (trong RAM). Khi bạn nối DB
 * cho thông báo, chỉ cần nạp dữ liệu từ DB vào {@link #addMessage(String)} là xong,
 * phần UI không phải đổi gì.
 */
public final class NotificationStore {

    private static final NotificationStore INSTANCE = new NotificationStore();

    /** Danh sách thông báo, mới nhất nằm trên cùng (index 0). */
    private final ObservableList<String> messages = FXCollections.observableArrayList();

    /** Số thông báo chưa đọc — dùng để hiện badge đỏ trên chuông. */
    private final IntegerProperty unreadCount = new SimpleIntegerProperty(0);

    private NotificationStore() { }

    public static NotificationStore getInstance() {
        return INSTANCE;
    }

    public ObservableList<String> getMessages() {
        return messages;
    }

    public IntegerProperty unreadCountProperty() {
        return unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount.get();
    }

    /** Thêm một thông báo mới lên đầu danh sách và tăng số chưa đọc. */
    public void addMessage(String message) {
        if (message == null || message.isBlank()) return;
        messages.add(0, message);
        unreadCount.set(unreadCount.get() + 1);
    }

    /** Đánh dấu đã đọc hết (gọi khi người dùng mở panel chuông). */
    public void markAllRead() {
        unreadCount.set(0);
    }

    /** Xoá sạch danh sách thông báo. */
    public void clear() {
        messages.clear();
        unreadCount.set(0);
    }
}
