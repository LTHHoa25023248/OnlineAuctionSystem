# Auction Management System - Mô tả chức năng

## Tổng quan
Project là một ứng dụng JavaFX desktop cho hệ thống đấu giá trực tuyến. Ứng dụng có giao diện nhiều màn hình và popup, được điều hướng bởi `NavigationUtil` và dùng `ThemeManager` để áp dụng style phù hợp.

## Chức năng chính

### 1. Đăng nhập và xác thực
- `LoginController` xử lý đăng nhập người dùng.
- Hỗ trợ đăng nhập bằng `username` và `password`.
- Kiểm tra lỗi dữ liệu đầu vào và hiển thị thông báo.
- Phân quyền người dùng theo role: `ADMIN`, `SELLER`, `BIDDER`.
- Sau khi đăng nhập thành công, chuyển sang trang danh sách đấu giá (`auction_list.fxml`).

### 2. Đăng ký tài khoản
- `SignupController` xử lý đăng ký người dùng mới.
- Cho phép chọn role `Buyer/Bidder` hoặc `Seller`.
- Validate: họ tên, username, email, số điện thoại, mật khẩu, xác nhận mật khẩu, đồng ý điều khoản.
- Gọi `UserDAO` để kiểm tra username/email tồn tại và lưu tài khoản.
- Chuyển về màn hình đăng nhập khi đăng ký thành công.

### 3. Quên mật khẩu
- `ForgotPassController` xử lý reset mật khẩu bằng email.
- Quy trình 3 bước: gửi mã OTP → xác thực mã → đặt mật khẩu mới.
- Sử dụng `EmailService` tạo và gửi mã OTP.
- Kiểm tra email tồn tại, lưu mã reset, xác thực OTP và đổi mật khẩu.

### 4. Danh sách đấu giá
- `AuctionListController` hiển thị danh sách các sản phẩm đấu giá.
- Có sidebar điều hướng: Home, Active listings, Trending, Your listings, Watchlist, Profile.
- Lọc theo category: Jewelry, Watches, Bags, Fine Art, Cars, Others.
- Hỗ trợ sort theo: Giá cao→thấp, Giá thấp→cao, Mới nhất, Cũ nhất, Sắp hết hạn, Còn nhiều thời gian.
- Cho phép đổi theme sáng/tối.
- Hiển thị nút `Sell` cho seller và `Admin` cho admin.
- Dữ liệu listing hiện tại là sample trong code (`getAllItems()`), chưa kết nối DB thực.

### 5. Thêm sản phẩm đấu giá
- `AddListingController` mở popup để seller tạo listing mới.
- Bắt buộc nhập tên sản phẩm, category, giá khởi điểm, ngày kết thúc.
- Cho phép chọn ảnh qua FileChooser.
- Tạo đối tượng `AuctionItem` tạm để hiển thị lên đầu danh sách sau khi đóng popup.
- TODO: hiện tại chưa lưu vào DB; sẽ cần gọi `AuctionDAO.create(...)` và lưu ảnh.

### 6. Chi tiết đấu giá
- `AuctionDetailController` hiển thị thông tin chi tiết sản phẩm và cơ chế đặt giá.
- Hiển thị thông tin như tên, giá, số lượng bid, thời gian, category, người đăng, mô tả.
- Hỗ trợ đặt giá (`Place Bid`) với validate số hợp lệ.
- Cho phép admin `End Now` kết thúc đấu giá sớm.
- Cho phép đăng bình luận trong section comment.
- Comment được render bởi `CommentController`.
- Dữ liệu hiện tại chứa sample và comment lưu tạm trong session.

### 7. Bình luận
- `CommentController` hiển thị một comment card.
- `AuctionDetailController` load nhiều comment động và hiển thị trong `commentsPane`.
- Comment hiện lưu tạm trong bộ nhớ, chưa lưu vào DB.

### 8. Hồ sơ người dùng
- `ProfileController` hiển thị thông tin người dùng trong popup profile.
- Hiển thị tên, username, email, số điện thoại.
- Hỗ trợ chuyển sang chế độ chỉnh sửa và lưu profile.
- Có nút `Deposit` mở popup nạp tiền.

### 9. Nạp tiền
- `DepositController` xử lý popup nạp tiền.
- Hiển thị số dư hiện tại và cho phép nhập hoặc chọn nhanh các giá trị preset.
- Validate số tiền: không trống, số dương, tối thiểu 10 USD, tối đa 100.000 USD.
- Cập nhật số dư tạm thời trong session.
- TODO: cần lưu giao dịch vào DB thật.

### 10. Bảng điều khiển Admin
- `AdminDashboardController` hiển thị dashboard quản trị cho admin.
- Bao gồm stat cards: tổng listings, active auctions, tổng users, doanh thu.
- Hiển thị bảng Listings và bảng Users có thể toggle.
- Có lọc auctions theo category và users theo trạng thái.
- Hiện tại dùng dữ liệu mẫu, chưa kết nối DAO thực cho admin dashboard.

### 11. Popup và điều hướng
- `NavigationUtil` quản lý chuyển màn hình và mở popup.
- Hỗ trợ `goTo()` thay đổi toàn bộ scene và `openPopup()` mở stage modal.
- Tự động xác định CSS tương ứng với loại màn hình qua `ThemeManager.CssType`.

## Các module khác
- `model/`: chứa các lớp dữ liệu như `User`, `Admin`, `Seller`, `Auction`, ...
- `dao/`: chứa các lớp truy xuất dữ liệu như `UserDAO`, `AuctionDAO`, `BidTransactionDAO`, `AutoBidDAO`, ...
- `service/`: chứa dịch vụ như `EmailService` và có thể cả logic hỗ trợ khác.
- `config/DatabaseConnection.java`: cấu hình kết nối MySQL.

## Công nghệ sử dụng
- Java 17
- JavaFX 17
- FXML + MaterialFX UI
- MySQL Connector/J
- Jakarta Mail để gửi email OTP
- Logback cho logging
- Maven để quản lý build

## Ghi chú quan trọng
- Nhiều phần hiện đang dùng dữ liệu mẫu hardcoded và chưa gọi DAO/DB thực sự.
- Các tính năng cần hoàn thiện: lưu listing mới, lấy dữ liệu auction thật, place bid vào DB, comment lưu DB, profile update, admin dashboard data, image storage.
- Cấu trúc đã có sẵn để mở rộng sang hệ thống DB đầy đủ.
