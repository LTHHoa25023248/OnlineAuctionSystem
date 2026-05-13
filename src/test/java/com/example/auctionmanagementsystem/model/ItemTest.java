package com.example.auctionmanagementsystem.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    // Kịch bản 1: Kiểm tra bẫy lỗi "Giá khởi điểm bị âm" (Nằm ở dòng 16 file Item.java)
    @Test
    public void testItemConstructor_NegativePrice_ShouldThrowException() {
        // Cố tình truyền giá trị -500.0
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Electronics("Laptop", "Cũ", -500.0, "Dell", 6);
        });

        assertTrue(exception.getMessage().contains("Starting price must greater than 0!"));
    }

    // Kịch bản 2: Kiểm tra hàm in thông tin đặc thù của đồ Điện tử (Override)
    @Test
    public void testElectronics_GetCategoryDetails_ShouldReturnCorrectFormat() {
        Electronics phone = new Electronics("iPhone 15", "Mới", 1000.0, "Apple", 12);

        String details = phone.getCategoryDetails();

        // So sánh với định dạng chuỗi String.format() mà bạn đã viết trong file Electronics.java
        assertEquals("Brand: Apple | Warranty: 12", details);
    }
}