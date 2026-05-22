// File: UserTest.java
package com.example.auctionmanagementsystem.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
  @Test
  public void testUserConstructor_ValidData_CreatesActiveUser() {
    // Vì User có chứa phương thức abstract printInfo(), ta phải ghi đè nó ở lớp vô danh
    User user = new User("nguyenvana", "pass123", "vana@gmail.com") {
      @Override
      public void printInfo() {}
    };

    // Kiểm tra dữ liệu được gán đúng
    assertEquals("nguyenvana", user.getUsername());
    assertEquals("pass123", user.getPassword());
    assertEquals("vana@gmail.com", user.getEmail());

    // Kiểm tra logic quan trọng: Mặc định tài khoản phải được kích hoạt
    assertTrue(user.isActive(), "Tài khoản mới tạo mặc định phải là Active (true)");
  }

  // Kiểm tra hàm Setter để đổi trạng thái tài khoản
  @Test
  public void testSetActive_ChangeStatus_UpdatesSuccessfully() {
    User user = new User("nguyenvanb", "pass456", "vanb@gmail.com") {
      @Override
      public void printInfo() {}
    };
    user.setActive(false);
    assertFalse(user.isActive(), "Tài khoản phải bị khóa (false) sau khi gọi setActive(false)");
  }
}
