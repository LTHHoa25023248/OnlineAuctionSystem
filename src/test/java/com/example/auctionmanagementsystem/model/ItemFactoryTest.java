package com.example.auctionmanagementsystem.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class ItemFactoryTest {
  @Test
  // Kiểm thử với tạo đối tượng kiểu Electronics
  public void testCreateItem() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("brand", "Dell");
    attributes.put("warrantyMonths", "12");

    Item item = ItemFactory.createItem("ELECTRONICS", "Laptop Dell", "Core i7", 150.0, attributes);

    // Sử dụng assertTrue(item instanceof Electronics) để xác minh tính Đa hình (Polymorphism) và
    // Factory Method Pattern
    assertNotNull(item, "Đối tượng không được để rỗng");
    assertTrue(item instanceof Electronics, "Thể hiện của loại Electronics");
    assertEquals("Laptop Dell", item.getName());
    assertEquals(150.0, item.getStartingPrice());

    Electronics electronics = (Electronics) item;
    electronics.getBrand();
  }

  @Test
  public void testCreateItem_NullType_ThrowsException() {
    // Kiểm tra check null cho type
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ItemFactory.createItem(null, "Sản phẩm", "Mô tả", 100.0, new HashMap<>());
    });
    assertEquals("You must enter product category!", exception.getMessage());
  }

  @Test
  public void testCreateItem_NullAttributes_ThrowsException() {
    // Kiểm tra check null cho attributes
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ItemFactory.createItem("Electronics", "Sản phẩm", "Mô tả", 100.0, null);
    });
    assertEquals("Please add more information!", exception.getMessage());
  }

  @Test
  public void testCreateItem_UnsupportedType_ThrowsException() {
    // Kiểm tra nhánh default trong switch-case
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ItemFactory.createItem("RealEstate", "Biệt thự", "Đẹp", 100.0, new HashMap<>());
    });
    assertEquals("Your product has not supported by our system yet!", exception.getMessage());
  }

  @Test
  public void testCreateItem_InvalidNumberFormat_ThrowsRuntimeException() {
    Map<String, String> attributes = new HashMap<>();
    attributes.put("brand", "Apple");
    attributes.put("warranty", "Mười hai tháng"); // Kiểm tra hàm parseInt

    // Kiểm tra khối try-catch
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ItemFactory.createItem("Electronics", "Macbook", "M1", 1000.0, attributes);
    });
    assertTrue(exception.getMessage().contains("Invalid number format"));
  }
}
