package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller cho View/AdminDashboard.fxml
 *
 * Điều hướng:
 *   handleHome      → auction_list.fxml
 *   handleAnalytics → chart.fxml (popup)
 *   handleLogout    → auction_login.fxml
 *   filter buttons  → reload table
 */
public class AdminDashboardController {

    @FXML private Label       usernameLabel;
    @FXML private Label       totalListingsLabel;
    @FXML private Label       activeAuctionsLabel;
    @FXML private Label       totalUsersLabel;
    @FXML private Label       revenueLabel;

    @FXML private TableView   listingsTable;
    @FXML private TableColumn colItem;
    @FXML private TableColumn colCategory;
    @FXML private TableColumn colSeller;
    @FXML private TableColumn colBid;
    @FXML private TableColumn colStatus;
    @FXML private TableColumn colActions;

    @FXML
    public void initialize() {
        usernameLabel.setText(SessionManager.getInstance().getUsername());
        loadStats();
        loadTable("ALL");
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    @FXML private void handleHome()      { NavigationUtil.goTo(usernameLabel, NavigationUtil.AUCTION_LIST); }
    @FXML private void handleListings()  { loadTable("ALL"); }
    @FXML private void handleUsers()     { /* TODO: màn hình quản lý user */ }
    @FXML private void handleAnalytics() { NavigationUtil.openPopup(usernameLabel, NavigationUtil.CHART, "Analytics"); }
    @FXML private void handleReports()   { /* TODO */ }
    @FXML private void handleSettings()  { /* TODO */ }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationUtil.goTo(usernameLabel, NavigationUtil.LOGIN);
    }

    // ── Filter buttons ────────────────────────────────────────────────────────
    @FXML private void filterAll()     { loadTable("ALL"); }
    @FXML private void filterJewelry() { loadTable("Jewelry"); }
    @FXML private void filterWatches() { loadTable("Watches"); }
    @FXML private void filterCars()    { loadTable("Cars"); }
    @FXML private void filterOthers()  { loadTable("Others"); }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadStats() {
        /* ── Thay bằng DAO thực ─────────────────────────────────────────────
         *   totalListingsLabel.setText(String.valueOf(AuctionDAO.count()));
         *   activeAuctionsLabel.setText(String.valueOf(AuctionDAO.countActive()));
         *   totalUsersLabel.setText(String.valueOf(UserDAO.count()));
         *   revenueLabel.setText("$" + AuctionDAO.totalRevenue());
         * ─────────────────────────────────────────────────────────────────── */
        totalListingsLabel.setText("128");
        activeAuctionsLabel.setText("34");
        totalUsersLabel.setText("512");
        revenueLabel.setText("$48,200");
    }

    @SuppressWarnings("unchecked")
    private void loadTable(String category) {
        /* ── Thay bằng DAO + setCellValueFactory ────────────────────────────
         *   colItem.setCellValueFactory(new PropertyValueFactory<>("title"));
         *   ...
         *   List<Auction> data = "ALL".equals(category)
         *       ? AuctionDAO.getAll() : AuctionDAO.getByCategory(category);
         *   listingsTable.setItems(FXCollections.observableArrayList(data));
         * ─────────────────────────────────────────────────────────────────── */
        System.out.println("Admin load table: " + category);
    }
}