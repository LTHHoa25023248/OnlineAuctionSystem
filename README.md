# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

> Bài tập lớn học phần **CS2043_16** — Trường Đại học Công nghệ, ĐHQGHN

---

## 1. Mô tả bài toán và phạm vi hệ thống

Hệ thống đấu giá trực tuyến là một nền tảng đa người dùng cho phép tổ chức và tham gia các phiên đấu giá tài sản theo thời gian thực qua mạng. Hệ thống được xây dựng theo mô hình **Client–Server** với 3 vai trò người dùng được phân quyền rõ ràng:

- **Bidder (Người đặt giá)**: xem danh sách phiên đấu giá đang mở, đặt giá thủ công hoặc đăng ký đấu giá tự động, theo dõi lịch sử trúng thưởng. Số dư khởi tạo 50.000 USD.
- **Seller (Người bán)**: đăng tải sản phẩm lên hệ thống, thiết lập giá khởi điểm và thời lượng, nhận tiền khi phiên kết thúc thành công.
- **Admin (Quản trị viên)**: phê duyệt hoặc từ chối phiên đấu giá, quản lý người dùng (cấm/bỏ cấm), kết thúc phiên thủ công và xem thống kê toàn hệ thống.

**Phạm vi nghiệp vụ** xoay quanh toàn bộ vòng đời một phiên đấu giá: từ giai đoạn `PENDING` (chờ duyệt) → `OPEN` (đang đấu giá) → `FINISHED`/`PAID` (kết thúc, thanh toán) hoặc `REJECTED`/`CANCELED`. Hệ thống hỗ trợ đấu giá đồng thời nhiều người dùng, cập nhật giá theo thời gian thực, đấu giá tự động kiểu Proxy Bidding (giống eBay) và chống chiến thuật giật giá phút cuối (Anti-Sniping).

---

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

### Công nghệ chính

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ lập trình | **Java 17+** |
| Giao diện đồ họa (Client) | **JavaFX 17.0.6** + FXML + CSS |
| Thư viện UI nâng cao | **MaterialFX 11.13.5** |
| HTTP Server (Server) | `com.sun.net.httpserver` (nhúng sẵn JDK, không cần Tomcat/Spring) |
| Cơ sở dữ liệu | **MySQL 8.0+** |
| JDBC Driver | mysql-connector-j 9.3.0 |
| Chuyển đổi JSON ↔ Object | **Gson 2.10.1** |
| Gửi email OTP | JavaMail (Jakarta Mail) |
| Kiểm thử | JUnit 5 + Mockito |
| Quản lý build | **Maven 3.8+** |
| CI/CD | GitHub Actions |

### Yêu cầu cài đặt môi trường

Trước khi chạy hệ thống, máy tính cần được cài đặt:

1. **JDK 17 trở lên** — kiểm tra bằng `java -version`
2. **Apache Maven 3.8+** — kiểm tra bằng `mvn -version`
3. **MySQL Server 8.0+** đang chạy — mặc định ở `localhost:3306`
4. **Git** để clone mã nguồn

### Chuẩn bị cơ sở dữ liệu

Trước khi khởi động Server lần đầu, cần tạo database MySQL và import schema:

```bash
# 1. Đăng nhập MySQL
mysql -u root -p

# 2. Tạo database
CREATE DATABASE auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 3. Import schema (file SQL trong thư mục dự án)
mysql -u root -p auction_db < database/auction_schema.sql
```

Sau đó cấu hình thông tin kết nối tại file `src/main/resources/db.properties` (hoặc trong lớp `DatabaseConnection.java`):

```
db.url=jdbc:mysql://localhost:3306/auction_db
db.user=root
db.password=YOUR_PASSWORD_HERE
```

---

## 3. Cấu trúc thư mục dự án

Mã nguồn được tổ chức theo kiến trúc phân tầng (Layered Architecture) kết hợp MVC:

```
auctionmanagementsystem/
├── pom.xml                          # Cấu hình Maven và dependencies
├── README.md
├── database/
│   └── auction_schema.sql           # Script khởi tạo cơ sở dữ liệu
├── .github/workflows/               # CI/CD pipeline (GitHub Actions)
└── src/
    ├── main/
    │   ├── java/com/auctionmanagementsystem/
    │   │   ├── model/               # Lớp dữ liệu: User, Bidder, Seller, Admin,
    │   │   │                        #   Auction, Item, BidTransaction, AutoBid…
    │   │   ├── dao/                 # Tầng truy cập DB: AuctionDAO, UserDAO,
    │   │   │                        #   ItemDAO, DatabaseConnection
    │   │   ├── service/             # Nghiệp vụ: AuctionService,
    │   │   │                        #   AdvancedAuctionService (Auto-Bid),
    │   │   │                        #   PaymentService, AuctionScheduler
    │   │   ├── server/
    │   │   │   ├── AuctionServer.java   # Điểm vào của Server, lắng nghe port 8080
    │   │   │   └── handler/             # AuthHandler, AuctionHandler,
    │   │   │                            #   UserHandler, AdminHandler, ItemHandler
    │   │   ├── client/
    │   │   │   ├── MainApp.java         # Điểm vào của Client (JavaFX Application)
    │   │   │   └── ApiClient.java       # Đóng gói HTTP request gửi tới Server
    │   │   ├── controller/          # JavaFX controllers (gắn với các file FXML)
    │   │   ├── observer/            # Observer Pattern: AuctionNotifier (Subject)
    │   │   └── exception/           # 8 lớp custom exception kế thừa RuntimeException
    │   └── resources/
    │       ├── fxml/                # 12 file giao diện FXML
    │       ├── css/                 # Stylesheet
    │       ├── images/              # Tài nguyên ảnh
    │       └── db.properties        # Cấu hình kết nối MySQL
    └── test/
        └── java/                    # Unit test JUnit 5 + Mockito
```

**Các tầng kiến trúc chính:**

| Tầng | Vai trò |
|---|---|
| Model | Đối tượng dữ liệu, mapping với bảng DB |
| DAO | Cô lập câu lệnh SQL, chuyển đổi DB ↔ Object |
| Service | Logic nghiệp vụ (đặt giá, phê duyệt, thanh toán, scheduler) |
| Handler | Tiếp nhận HTTP request, kiểm tra và điều hướng |
| Observer | Phát sự kiện realtime tới Client đang theo dõi |
| Controller | Liên kết FXML với logic ứng dụng phía Client |

---

## 4. Hướng dẫn build và chạy chương trình

> Các lệnh dưới đây hoạt động trên **Linux, macOS và Windows**. Trên Linux/macOS dùng terminal thông thường; trên Windows dùng **PowerShell** hoặc **Git Bash**.

### Bước 1 — Clone mã nguồn

```bash
git clone <repository-url>
cd auctionmanagementsystem
```

### Bước 2 — Build dự án với Maven

Lệnh này tải toàn bộ dependency và biên dịch mã nguồn:

```bash
mvn clean install -DskipTests
```

Nếu muốn chạy cả unit test trong quá trình build:

```bash
mvn clean install
```

### Bước 3 — Khởi động Server (chạy trước Client)

Mở **một cửa sổ terminal riêng** và chạy:

```bash
mvn clean test-compile exec:java "-Dexec.mainClass=com.example.auctionmanagementsystem.Main"
```

Server sẽ lắng nghe ở `http://localhost:8080`. Khi thấy log `Server started on port 8080` là đã sẵn sàng. **Giữ cửa sổ này luôn mở** trong suốt thời gian sử dụng hệ thống.

### Bước 4 — Khởi động Client (sau khi Server đã chạy)

Mở **một cửa sổ terminal khác** và chạy:

```bash
mvn javafx:run
```

Cửa sổ JavaFX sẽ hiện ra, hiển thị màn hình đăng nhập. Có thể mở nhiều Client cùng lúc để mô phỏng đấu giá đa người dùng.

### Chạy unit test

```bash
mvn test
```

### Lưu ý theo hệ điều hành

- **Linux/macOS**: nếu gặp lỗi `Permission denied` khi chạy script, cấp quyền: `chmod +x mvnw` rồi dùng `./mvnw` thay cho `mvn`.
- **macOS với Apple Silicon (M1/M2/M3)**: cần JavaFX bản `aarch64` — Maven sẽ tự chọn nhờ classifier trong `pom.xml`.
- **Windows**: nếu cổng 8080 bị chiếm, kiểm tra bằng `netstat -ano | findstr :8080`; trên Linux/macOS dùng `lsof -i :8080`.

---

## 5. Danh sách chức năng đã hoàn thành

### Chức năng bắt buộc

- [x] **Quản lý tài khoản & người dùng**: đăng ký, đăng nhập, phân quyền 3 vai trò (Bidder/Seller/Admin)
- [x] **Quản lý sản phẩm**: Seller đăng sản phẩm, chỉnh sửa, đặt giá khởi điểm và thời gian
- [x] **Phê duyệt phiên đấu giá**: Admin duyệt hoặc từ chối (kèm lý do) trước khi phiên được công khai
- [x] **Đặt giá thủ công**: kiểm tra hợp lệ (giá > giá hiện tại, phiên đang OPEN, người dùng đã đăng nhập)
- [x] **Logic vòng đời phiên đấu giá** với 6 trạng thái: `PENDING`, `OPEN`, `FINISHED`, `PAID`, `REJECTED`, `CANCELED`
- [x] **Xử lý đấu giá đồng thời an toàn**: dùng `ReentrantLock` cho mỗi phiên, kết hợp DB transaction tránh race condition
- [x] **Realtime update**: cập nhật giá mới cho mọi Client đang xem phiên thông qua **Observer Pattern** (`AuctionNotifier`)
- [x] **Tự động đóng phiên**: daemon thread (`ScheduledExecutorService`) chạy mỗi 30 giây, tự kết thúc phiên hết giờ
- [x] **Thanh toán tự động**: khi phiên đóng có người thắng → chuyển tiền từ Bidder sang Seller, cập nhật trạng thái `PAID`
- [x] **Xử lý ngoại lệ chuyên biệt**: 8 lớp custom exception kế thừa `RuntimeException`, trả thông báo lỗi đẹp về Client
- [x] **Kiến trúc Client–Server rõ ràng**: HTTP REST API qua port 8080, dữ liệu JSON
- [x] **Áp dụng MVC**: 12 file FXML + Controller-Model-DAO phía Client
- [x] **Design Patterns**: Singleton (AuctionNotifier, SessionManager, AuctionScheduler), Observer, DAO, Factory
- [x] **OOP đầy đủ**: Encapsulation, Inheritance (User → Bidder/Seller/Admin), Polymorphism, Abstraction
- [x] **Unit Test với JUnit 5** + Mockito giả lập tầng DAO
- [x] **CI/CD GitHub Actions**: tự động chạy `mvn test` mỗi khi push code
- [x] **Maven + Coding Convention**: tuân thủ Google Java Style Guide

### Chức năng nâng cao (tùy chọn)

- [x] **Đấu giá tự động (Proxy Bidding / Auto-Bid)**: người dùng đặt `maxBid` + `increment`, hệ thống tự trả giá vừa đủ để dẫn đầu, không bao giờ trả vượt mức tối đa
- [x] **Chống giật giá (Anti-Sniping)**: nếu có bid trong 10 giây cuối, tự động gia hạn thêm 60 giây và thông báo cho tất cả
- [x] **Biểu đồ lịch sử giá realtime**: dùng `LineChart` của JavaFX, tự động cập nhật khi có bid mới (không cần F5)
- [x] **Admin Dashboard**: thống kê toàn hệ thống — tổng số người dùng, phiên đang chạy, doanh thu, top sellers
- [x] **Trang cá nhân**: chỉnh sửa họ tên, số điện thoại; hiển thị số dư ví, vai trò, lịch sử giao dịch
- [x] **Quên mật khẩu qua OTP email**: luồng 3 bước (gửi mã 6 số → xác thực → đổi mật khẩu); OTP hết hạn sau 10 phút, chống spam 60 giây giữa các lần gửi
- [x] **Cấm / bỏ cấm tài khoản**: Admin có thể vô hiệu hóa tài khoản vi phạm (`is_active = false`)

---

## 6. Tài liệu và demo

- **Link báo cáo dự án và video demo**: https://drive.google.com/drive/folders/1gu3TQLSAoypNHSKvWXpkQLgy7YAxwj9s?usp=sharing

---

## 7. Thành viên nhóm

| Họ tên | MSSV | Vai trò |
|---|---|---|
| Nguyễn Mai Hồng Diệp | 25023186 | Thiết kế hệ thống sản phẩm, kiểm thử và tích hợp |
| Nguyễn Minh Đức | 25023217 | Thiết kế giao diện người dùng |
| Lê Thị Hồng Hoa | 25023248 | Thiết kế nghiệp vụ đấu giá và mô hình Client–Server |
| Phạm Đức Huy | 25023270 | Realtime-update và quản lý cơ sở dữ liệu |

---

*Học phần: 2526II_CS2043_16 — Trường Đại học Công nghệ, ĐHQGHN*
