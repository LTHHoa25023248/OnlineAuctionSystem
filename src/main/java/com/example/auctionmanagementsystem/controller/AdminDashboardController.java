package com.example.auctionmanagementsystem.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * AdminDashboardController — Màn hình quản trị (AdminDashboard.fxml).
 *
 * Chỉ truy cập được nếu SessionManager.isAdmin() == true.
 * Mở từ AuctionListController (adminButton) — chuyển trang, không phải popup.
 *
 * Tính năng:
 *   - Stat cards: tổng listing, active auctions, tổng users, doanh thu
 *   - Bảng Listings: filter theo category, xem/sửa từng item
 *   - Bảng Users: toggle hiện/ẩn, filter theo role/status
 *
 * TODO: Thay dữ liệu mẫu bằng AuctionDAO và UserDAO thật.
 */
public class AdminDashboardController {

    // ── FXML fields — Top bar ─────────────────────────────────────────────────
    @FXML private Label usernameLabel;

    // ── FXML fields — Stat cards ──────────────────────────────────────────────
    @FXML private Label totalListingsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label revenueLabel;

    // ── FXML fields — Listings table ─────────────────────────────────────────
    @FXML private TableView<AuctionRow>           listingsTable;
    @FXML private TableColumn<AuctionRow, String> colItem;
    @FXML private TableColumn<AuctionRow, String> colCategory;
    @FXML private TableColumn<AuctionRow, String> colSeller;
    @FXML private TableColumn<AuctionRow, String> colBid;
    @FXML private TableColumn<AuctionRow, String> colStatus;
    @FXML private TableColumn<AuctionRow, String> colActions;

    // ── FXML fields — Users table (ẩn mặc định) ──────────────────────────────
    @FXML private VBox                         usersCard;  // container ẩn/hiện
    @FXML private TableView<UserRow>           usersTable;
    @FXML private TableColumn<UserRow, String> colUserId;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colEmail;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, String> colUserStatus;
    @FXML private TableColumn<UserRow, String> colJoinDate;

    // ── Inner classes: Row models cho TableView ───────────────────────────────

    /**
     * Model một hàng trong bảng Listings.
     * JavaFX TableView yêu cầu getter cho mỗi cột (PropertyValueFactory).
     */
    public static class AuctionRow {
        private final StringProperty item, category, seller, bid, status, actions;

        public AuctionRow(String item, String category, String seller,
                          String bid, String status, String actions) {
            this.item     = new SimpleStringProperty(item);
            this.category = new SimpleStringProperty(category);
            this.seller   = new SimpleStringProperty(seller);
            this.bid      = new SimpleStringProperty(bid);
            this.status   = new SimpleStringProperty(status);
            this.actions  = new SimpleStringProperty(actions);
        }

        // Getters — phải đúng tên để PropertyValueFactory tìm được
        public String getItem()     { return item.get(); }
        public String getCategory() { return category.get(); }
        public String getSeller()   { return seller.get(); }
        public String getBid()      { return bid.get(); }
        public String getStatus()   { return status.get(); }
        public String getActions()  { return actions.get(); }
    }

    /**
     * Model một hàng trong bảng Users.
     */
    public static class UserRow {
        private final StringProperty userId, username, email, role, status, joinDate;

        public UserRow(String userId, String username, String email,
                       String role, String status, String joinDate) {
            this.userId   = new SimpleStringProperty(userId);
            this.username = new SimpleStringProperty(username);
            this.email    = new SimpleStringProperty(email);
            this.role     = new SimpleStringProperty(role);
            this.status   = new SimpleStringProperty(status);
            this.joinDate = new SimpleStringProperty(joinDate);
        }

        public String getUserId()   { return userId.get(); }
        public String getUsername() { return username.get(); }
        public String getEmail()    { return email.get(); }
        public String getRole()     { return role.get(); }
        public String getStatus()   { return status.get(); }
        public String getJoinDate() { return joinDate.get(); }
    }

    // ── Initialize ────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        usernameLabel.setText(SessionManager.getInstance().getUsername());
        loadStats();
        setupTable();
        loadTable("ALL");   // load bảng listing mặc định
        setupUsersTable();  // chuẩn bị cột, chưa load data
    }

    // ── Sidebar handlers ──────────────────────────────────────────────────────

    /** Quay về màn hình danh sách auction */
    @FXML private void handleHome()     { NavigationUtil.goTo(usernameLabel, NavigationUtil.AUCTION_LIST); }

    /** Reload bảng listing toàn bộ */
    @FXML private void handleListings() { loadTable("ALL"); }

    /**
     * Toggle hiển thị bảng Users.
     * Lần đầu mở sẽ load dữ liệu.
     */
    @FXML
    private void handleUsers() {
        boolean show = !usersCard.isVisible();
        usersCard.setVisible(show);
        usersCard.setManaged(show);
        if (show) loadUsersTable("ALL");
    }

    /** Mở popup biểu đồ Analytics */
    @FXML private void handleAnalytics() {
        NavigationUtil.openPopup(usernameLabel, NavigationUtil.CHART, "Analytics");
    }

    /** Đăng xuất và về trang Login */
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.goTo(usernameLabel, NavigationUtil.LOGIN);
    }

    // ── Auction filter buttons ────────────────────────────────────────────────

    @FXML private void filterAll()     { loadTable("ALL"); }
    @FXML private void filterJewelry() { loadTable("Jewelry"); }
    @FXML private void filterWatches() { loadTable("Watches"); }
    @FXML private void filterCars()    { loadTable("Cars"); }
    @FXML private void filterOthers()  { loadTable("Others"); }

    // ── Users filter buttons ──────────────────────────────────────────────────

    @FXML private void filterUsersAll()    { loadUsersTable("ALL"); }
    @FXML private void filterUsersActive() { loadUsersTable("Active"); }
    @FXML private void filterUsersBanned() { loadUsersTable("Banned"); }
    @FXML private void filterUsersAdmin()  { loadUsersTable("Admin"); }

    // ── Setup column mappings ─────────────────────────────────────────────────

    /**
     * Kết nối cột TableView với getter của AuctionRow.
     * PropertyValueFactory("item") tìm getItem() trong AuctionRow.
     */
    private void setupTable() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSeller.setCellValueFactory(new PropertyValueFactory<>("seller"));
        colBid.setCellValueFactory(new PropertyValueFactory<>("bid"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colActions.setCellValueFactory(new PropertyValueFactory<>("actions"));
    }

    private void setupUsersTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colJoinDate.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
    }

    // ── Stats (TODO: thay bằng DAO thật) ─────────────────────────────────────

    private void loadStats() {
        totalListingsLabel.setText("128");
        activeAuctionsLabel.setText("34");
        totalUsersLabel.setText("512");
        revenueLabel.setText("48,200 USD");
    }

    // ── Listings data ─────────────────────────────────────────────────────────

    /**
     * Load dữ liệu vào bảng Listings, filter theo category nếu cần.
     * TODO: Thay bằng AuctionDAO.getAll() hoặc AuctionDAO.getByCategory(category).
     *
     * @param category "ALL" để lấy tất cả, hoặc tên category cụ thể
     */
    private void loadTable(String category) {
        ObservableList<AuctionRow> data = FXCollections.observableArrayList();

        // Dữ liệu mẫu — mỗi phần tử: {tên, category, seller, giá, status, action}
        Object[][] rows = {
                {"Rolex Daytona",       "Jewelry",  "alice",   "15,000 USD",  "Active", "Edit"},
                {"Diamond Ring",        "Jewelry",  "bob",     "8,500 USD",   "Active", "Edit"},
                {"Pearl Necklace",      "Jewelry",  "carol",   "3,200 USD",   "Ended",  "View"},
                {"Omega Seamaster",     "Watches",  "dave",    "5,400 USD",   "Active", "Edit"},
                {"Patek Philippe",      "Watches",  "eve",     "22,000 USD",  "Active", "Edit"},
                {"TAG Heuer Monaco",    "Watches",  "frank",   "4,100 USD",   "Ended",  "View"},
                {"Hermes Birkin",       "Bags",     "grace",   "12,000 USD",  "Active", "Edit"},
                {"Chanel Flap",         "Bags",     "heidi",   "7,800 USD",   "Active", "Edit"},
                {"Porsche 911",         "Cars",     "judy",    "95,000 USD",  "Active", "Edit"},
                {"Ferrari 488",         "Cars",     "kevin",   "180,000 USD", "Active", "Edit"},
                {"Lamborghini Huracan", "Cars",     "laura",   "220,000 USD", "Ended",  "View"},
                {"Mona Lisa Print",     "Fine Art", "mike",    "45,000 USD",  "Active", "Edit"},
                {"Vintage Guitar",      "Others",   "oscar",   "2,800 USD",   "Active", "Edit"},
                {"Antique Clock",       "Others",   "peggy",   "1,500 USD",   "Ended",  "View"},
                {"Rare Coin",           "Others",   "quentin", "900 USD",     "Active", "Edit"},
        };

        for (Object[] row : rows) {
            String cat = (String) row[1];
            if (category.equals("ALL") || cat.equalsIgnoreCase(category)) {
                data.add(new AuctionRow(
                        (String) row[0], cat, (String) row[2],
                        (String) row[3], (String) row[4], (String) row[5]));
            }
        }
        listingsTable.setItems(data);
    }

    // ── Users data ────────────────────────────────────────────────────────────

    /**
     * Load dữ liệu vào bảng Users, filter theo role hoặc status.
     * TODO: Thay bằng UserDAO.getAll() hoặc UserDAO.getByStatus(filter).
     *
     * @param filter "ALL", "Active", "Banned", hoặc "Admin"
     */
    private void loadUsersTable(String filter) {
        ObservableList<UserRow> data = FXCollections.observableArrayList();

        Object[][] users = {
                {"1",  "alice", "alice@demo.com", "User",  "Active", "01/01/2024"},
                {"2",  "bob",   "bob@demo.com",   "User",  "Active", "05/02/2024"},
                {"3",  "carol", "carol@demo.com", "User",  "Banned", "10/03/2024"},
                {"4",  "dave",  "dave@demo.com",  "User",  "Active", "15/03/2024"},
                {"5",  "eve",   "eve@demo.com",   "Admin", "Active", "20/04/2024"},
                {"6",  "frank", "frank@demo.com", "User",  "Active", "01/05/2024"},
                {"7",  "grace", "grace@demo.com", "User",  "Banned", "10/05/2024"},
                {"8",  "heidi", "heidi@demo.com", "User",  "Active", "15/06/2024"},
                {"9",  "ivan",  "ivan@demo.com",  "Admin", "Active", "20/07/2024"},
                {"10", "judy",  "judy@demo.com",  "User",  "Active", "25/08/2024"},
        };

        for (Object[] u : users) {
            String role   = (String) u[3];
            String status = (String) u[4];
            // Match nếu là ALL, hoặc khớp status, hoặc khớp role
            boolean match = filter.equals("ALL")
                    || filter.equalsIgnoreCase(status)
                    || filter.equalsIgnoreCase(role);
            if (match) {
                data.add(new UserRow(
                        (String) u[0], (String) u[1], (String) u[2],
                        role, status, (String) u[5]));
            }
        }
        usersTable.setItems(data);
    }
}