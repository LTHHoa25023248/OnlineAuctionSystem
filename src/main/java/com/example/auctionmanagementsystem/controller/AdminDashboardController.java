package com.example.auctionmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AdminDashboardController {

    @FXML private Label usernameLabel;
    @FXML private Label totalListingsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label revenueLabel;

    // ── Auction Table ─────────────────────────────────────────────────────────
    @FXML private TableView<AuctionRow>           listingsTable;
    @FXML private TableColumn<AuctionRow, String> colItem;
    @FXML private TableColumn<AuctionRow, String> colCategory;
    @FXML private TableColumn<AuctionRow, String> colSeller;
    @FXML private TableColumn<AuctionRow, String> colBid;
    @FXML private TableColumn<AuctionRow, String> colStatus;
    @FXML private TableColumn<AuctionRow, String> colActions;

    // ── Users Table ───────────────────────────────────────────────────────────
    @FXML private VBox                         usersCard;
    @FXML private TableView<UserRow>           usersTable;
    @FXML private TableColumn<UserRow, String> colUserId;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colEmail;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, String> colUserStatus;
    @FXML private TableColumn<UserRow, String> colJoinDate;

    // ── AuctionRow ────────────────────────────────────────────────────────────
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

        public String getItem()     { return item.get(); }
        public String getCategory() { return category.get(); }
        public String getSeller()   { return seller.get(); }
        public String getBid()      { return bid.get(); }
        public String getStatus()   { return status.get(); }
        public String getActions()  { return actions.get(); }
    }

    // ── UserRow ───────────────────────────────────────────────────────────────
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
        loadTable("ALL");
        setupUsersTable();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private void handleHome()     { NavigationUtil.goTo(usernameLabel, NavigationUtil.AUCTION_LIST); }
    @FXML private void handleListings() { loadTable("ALL"); }

    @FXML
    private void handleUsers() {
        boolean show = !usersCard.isVisible();
        usersCard.setVisible(show);
        usersCard.setManaged(show);
        if (show) loadUsersTable("ALL");
    }

    @FXML private void handleAnalytics() { NavigationUtil.openPopup(usernameLabel, NavigationUtil.CHART, "Analytics"); }
    @FXML private void handleReports()   { /* TODO */ }
    @FXML private void handleSettings()  { /* TODO */ }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.goTo(usernameLabel, NavigationUtil.LOGIN);
    }

    // ── Auction filter ────────────────────────────────────────────────────────
    @FXML private void filterAll()     { loadTable("ALL"); }
    @FXML private void filterJewelry() { loadTable("Jewelry"); }
    @FXML private void filterWatches() { loadTable("Watches"); }
    @FXML private void filterCars()    { loadTable("Cars"); }
    @FXML private void filterOthers()  { loadTable("Others"); }

    // ── Users filter ──────────────────────────────────────────────────────────
    @FXML private void filterUsersAll()    { loadUsersTable("ALL"); }
    @FXML private void filterUsersActive() { loadUsersTable("Active"); }
    @FXML private void filterUsersBanned() { loadUsersTable("Banned"); }
    @FXML private void filterUsersAdmin()  { loadUsersTable("Admin"); }

    // ── Setup columns ─────────────────────────────────────────────────────────
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

    // ── Stats ─────────────────────────────────────────────────────────────────
    private void loadStats() {
        totalListingsLabel.setText("128");
        activeAuctionsLabel.setText("34");
        totalUsersLabel.setText("512");
        revenueLabel.setText("$48,200");
    }

    // ── Auction data ──────────────────────────────────────────────────────────
    private void loadTable(String category) {
        ObservableList<AuctionRow> data = FXCollections.observableArrayList();

        Object[][] rows = {
                {"Rolex Daytona",       "Jewelry",  "alice",   "$ 15,000",  "Active", "Edit | End"},
                {"Diamond Ring",        "Jewelry",  "bob",     "$ 8,500",   "Active", "Edit | End"},
                {"Pearl Necklace",      "Jewelry",  "carol",   "$ 3,200",   "Ended",  "View"},
                {"Omega Seamaster",     "Watches",  "dave",    "$ 5,400",   "Active", "Edit | End"},
                {"Patek Philippe",      "Watches",  "eve",     "$ 22,000",  "Active", "Edit | End"},
                {"TAG Heuer Monaco",    "Watches",  "frank",   "$ 4,100",   "Ended",  "View"},
                {"Hermes Birkin",       "Bags",     "grace",   "$ 12,000",  "Active", "Edit | End"},
                {"Chanel Flap",         "Bags",     "heidi",   "$ 7,800",   "Active", "Edit | End"},
                {"Louis Vuitton",       "Bags",     "ivan",    "$ 3,500",   "Ended",  "View"},
                {"Porsche 911",         "Cars",     "judy",    "$ 95,000",  "Active", "Edit | End"},
                {"Ferrari 488",         "Cars",     "kevin",   "$ 180,000", "Active", "Edit | End"},
                {"Lamborghini Huracan", "Cars",     "laura",   "$ 220,000", "Ended",  "View"},
                {"Mona Lisa Print",     "Fine Art", "mike",    "$ 45,000",  "Active", "Edit | End"},
                {"Starry Night",        "Fine Art", "nancy",   "$ 38,000",  "Active", "Edit | End"},
                {"Vintage Guitar",      "Others",   "oscar",   "$ 2,800",   "Active", "Edit | End"},
                {"Antique Clock",       "Others",   "peggy",   "$ 1,500",   "Ended",  "View"},
                {"Rare Coin",           "Others",   "quentin", "$ 900",     "Active", "Edit | End"},
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
        System.out.println("Admin load table: " + category);
    }

    // ── Users data ────────────────────────────────────────────────────────────
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
        System.out.println("Users load: " + filter);
    }
}