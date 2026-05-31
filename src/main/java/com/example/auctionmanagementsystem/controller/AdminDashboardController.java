package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Map;

public class AdminDashboardController {

  // FXML fields — Top bar 
  @FXML private Label usernameLabel;

  // FXML fields — Stat cards ─
  @FXML private Label totalListingsLabel;
  @FXML private Label activeAuctionsLabel;
  @FXML private Label totalUsersLabel;
  @FXML private Label revenueLabel;

  // FXML fields — Listings table
  @FXML private TableView<AuctionRow> listingsTable;
  @FXML private TableColumn<AuctionRow, String> colItem;
  @FXML private TableColumn<AuctionRow, String> colCategory;
  @FXML private TableColumn<AuctionRow, String> colSeller;
  @FXML private TableColumn<AuctionRow, String> colBid;
  @FXML private TableColumn<AuctionRow, String> colStatus;
  @FXML private TableColumn<AuctionRow, String> colActions;

  // FXML fields — Sidebar nav items 
  @FXML private HBox navListings;
  @FXML private HBox navUsers;

  // FXML fields — Listings card 
  @FXML private VBox listingsCard;

  // FXML fields — Users table ─
  @FXML private VBox usersCard;
  @FXML private TableView<UserRow> usersTable;
  @FXML private TableColumn<UserRow, String> colUserId;
  @FXML private TableColumn<UserRow, String> colUsername;
  @FXML private TableColumn<UserRow, String> colEmail;
  @FXML private TableColumn<UserRow, String> colRole;
  @FXML private TableColumn<UserRow, String> colUserStatus;
  @FXML private TableColumn<UserRow, String> colJoinDate;
  @FXML private TableColumn<UserRow, String> colUserActions;

  // Inner classes: Row models 

  public static class AuctionRow {
    private final int auctionId;
    private final StringProperty item, category, seller, bid, status, rejectReason;

    public AuctionRow(int auctionId, String item, String category, String seller,
                      String bid, String status, String rejectReason) {
      this.auctionId    = auctionId;
      this.item         = new SimpleStringProperty(item);
      this.category     = new SimpleStringProperty(category);
      this.seller       = new SimpleStringProperty(seller);
      this.bid          = new SimpleStringProperty(bid);
      this.status       = new SimpleStringProperty(status);
      this.rejectReason = new SimpleStringProperty(rejectReason != null ? rejectReason : "");
    }

    public int    getAuctionId()    { return auctionId; }
    public String getItem()         { return item.get(); }
    public String getCategory()     { return category.get(); }
    public String getSeller()       { return seller.get(); }
    public String getBid()          { return bid.get(); }
    public String getStatus()       { return status.get(); }
    public String getRejectReason() { return rejectReason.get(); }
  }

  public static class UserRow {
    private final int userIdInt;
    private final boolean active;
    private final StringProperty userId, username, email, role, status, joinDate;

    public UserRow(int userIdInt, String username, String email,
                   String role, boolean active, String joinDate) {
      this.userIdInt = userIdInt;
      this.active    = active;
      this.userId    = new SimpleStringProperty(String.valueOf(userIdInt));
      this.username  = new SimpleStringProperty(username);
      this.email     = new SimpleStringProperty(email);
      this.role      = new SimpleStringProperty(role);
      this.status    = new SimpleStringProperty(active ? "Active" : "Banned");
      this.joinDate  = new SimpleStringProperty(joinDate);
    }

    public int     getUserIdInt() { return userIdInt; }
    public boolean isActive()     { return active; }
    public String  getUserId()    { return userId.get(); }
    public String  getUsername()  { return username.get(); }
    public String  getEmail()     { return email.get(); }
    public String  getRole()      { return role.get(); }
    public String  getStatus()    { return status.get(); }
    public String  getJoinDate()  { return joinDate.get(); }
  }

  // Initialize 

  @FXML
  public void initialize() {
    usernameLabel.setText(SessionManager.getInstance().getUsername());
    setupTable();
    setupUsersTable();
    loadStats();
    showListings();
  }

  // Sidebar handlers ─

  @FXML private void handleHome()      { NavigationUtil.goTo(usernameLabel, NavigationUtil.AUCTION_LIST); }
  @FXML private void handleAnalytics() { }

  @FXML
  private void handleListings() {
    showListings();
  }

  @FXML
  private void handleUsers() {
    showUsers();
  }

  private void showListings() {
    if (listingsCard != null) { listingsCard.setVisible(true);  listingsCard.setManaged(true); }
    if (usersCard    != null) { usersCard.setVisible(false);    usersCard.setManaged(false); }
    setNavActive(navListings, navUsers);
    loadStats();
    loadTable("ALL");
  }

  private void showUsers() {
    if (usersCard    != null) { usersCard.setVisible(true);     usersCard.setManaged(true); }
    if (listingsCard != null) { listingsCard.setVisible(false); listingsCard.setManaged(false); }
    setNavActive(navUsers, navListings);
    loadStats();
    loadUsersTable("ALL");
  }

  private void setNavActive(HBox active, HBox inactive) {
    if (active   != null) { active.getStyleClass().remove("navActive");   active.getStyleClass().add("navActive"); }
    if (inactive != null) { inactive.getStyleClass().remove("navActive"); }
  }

  @FXML
  private void handleLogout() {
    SessionManager.getInstance().logout();
    NavigationUtil.goTo(usernameLabel, NavigationUtil.LOGIN);
  }

  // Auction filter buttons
  @FXML private void filterAll()        { loadTable("ALL"); }
  @FXML private void filterPending()    { loadTable("PENDING"); }
  @FXML private void filterElectronics(){ loadTable("ELECTRONICS"); }
  @FXML private void filterArt()        { loadTable("ART"); }
  @FXML private void filterVehicle()    { loadTable("VEHICLE"); }

  // Users filter buttons 
  @FXML private void filterUsersAll()    { loadUsersTable("ALL"); }
  @FXML private void filterUsersActive() { loadUsersTable("Active"); }
  @FXML private void filterUsersBanned() { loadUsersTable("Banned"); }
  @FXML private void filterUsersAdmin()  { loadUsersTable("Admin"); }

  // Setup column mappings 

  private void setupTable() {
    if (colItem     != null) colItem.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItem()));
    if (colCategory != null) colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
    if (colSeller   != null) colSeller.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeller()));
    if (colBid      != null) colBid.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBid()));
    if (colStatus   != null) colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

    if (colActions != null) {
      colActions.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
      colActions.setCellFactory(col -> new TableCell<>() {
        private final Button btnApprove = new Button("Approve");
        private final Button btnReject  = new Button("Reject");
        private final HBox   box        = new HBox(6, btnApprove, btnReject);

        {
          btnApprove.setStyle("-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:3 8;");
          btnReject.setStyle("-fx-background-color:#c62828;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:3 8;");

          btnApprove.setOnAction(e -> {
            AuctionRow row = getTableView().getItems().get(getIndex());
            approveAuction(row.getAuctionId());
          });
          btnReject.setOnAction(e -> {
            AuctionRow row = getTableView().getItems().get(getIndex());
            rejectAuction(row.getAuctionId());
          });
        }

        @Override
        protected void updateItem(String status, boolean empty) {
          super.updateItem(status, empty);
          if (empty || status == null) {
            setGraphic(null);
            setText(null);
          } else if ("PENDING".equals(status)) {
            setGraphic(box);
            setText(null);
          } else {
            setGraphic(null);
            setText("—");
          }
        }
      });
    }
  }

  private void setupUsersTable() {
    if (colUserId     != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
    if (colUsername   != null) colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    if (colEmail      != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    if (colRole       != null) colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
    if (colUserStatus != null) colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    if (colJoinDate   != null) colJoinDate.setCellValueFactory(new PropertyValueFactory<>("joinDate"));

    if (colUserActions != null) {
      colUserActions.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
      colUserActions.setCellFactory(col -> new TableCell<>() {
        private final Button btn = new Button();
        {
          btn.setOnAction(e -> {
            UserRow row = getTableView().getItems().get(getIndex());
            if (row.isActive()) {
              banUser(row.getUserIdInt());
            } else {
              unbanUser(row.getUserIdInt());
            }
          });
        }

        @Override
        protected void updateItem(String status, boolean empty) {
          super.updateItem(status, empty);
          if (empty || status == null) {
            setGraphic(null);
          } else {
            boolean isActive = "Active".equals(status);
            btn.setText(isActive ? "Ban" : "Unban");
            btn.setStyle(isActive
                ? "-fx-background-color:#c62828;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:3 10;"
                : "-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:3 10;");
            setGraphic(btn);
          }
        }
      });
    }
  }

  // Stats 

  private void loadStats() {
    Task<JsonObject> task = new Task<>() {
      @Override
      protected JsonObject call() throws Exception {
        return ApiClient.getObject("/admin/stats");
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      JsonObject s = task.getValue();
      if (totalListingsLabel  != null) totalListingsLabel.setText(s.get("totalListings").getAsString());
      if (activeAuctionsLabel != null) activeAuctionsLabel.setText(s.get("activeAuctions").getAsString());
      if (totalUsersLabel     != null) totalUsersLabel.setText(s.get("totalUsers").getAsString());
      if (revenueLabel        != null) revenueLabel.setText(
          String.format("%,.0f USD", s.get("revenue").getAsDouble()));
    }));
    task.setOnFailed(e -> {
      if (totalListingsLabel != null) totalListingsLabel.setText("N/A");
    });
    new Thread(task).start();
  }

  // Listings data 

  private void loadTable(String filter) {
    Task<ObservableList<AuctionRow>> task = new Task<>() {
      @Override
      protected ObservableList<AuctionRow> call() throws Exception {
        ObservableList<AuctionRow> data = FXCollections.observableArrayList();
        JsonArray arr = ApiClient.getArray("/admin/auctions?filter=" + filter);
        for (JsonElement el : arr) {
          JsonObject obj = el.getAsJsonObject();
          
          // ID
          int id = (obj.has("id") && !obj.get("id").isJsonNull()) ? obj.get("id").getAsInt() : 0;
          
          // Name
          String name = (obj.has("name") && !obj.get("name").isJsonNull()) ? obj.get("name").getAsString() : "N/A";
          
          // Type / Category (Sửa "itemType" thành "category" theo đúng JSON)
          String type = (obj.has("category") && !obj.get("category").isJsonNull()) ? obj.get("category").getAsString() : "";
          
          // Seller
          String seller = (obj.has("seller") && !obj.get("seller").isJsonNull()) ? obj.get("seller").getAsString() : "N/A";
          
          // Price (Sửa "currentPrice" thành "price", và lấy trực tiếp dạng String)
          String price = (obj.has("price") && !obj.get("price").isJsonNull()) 
                         ? obj.get("price").getAsString() 
                         : "N/A";
                         
          // Status
          String status = (obj.has("status") && !obj.get("status").isJsonNull()) ? obj.get("status").getAsString() : "";
          
          // Reject Reason
          String reason = (obj.has("rejectReason") && !obj.get("rejectReason").isJsonNull()) ? obj.get("rejectReason").getAsString() : "";
          
          // Xử lý Cat Label cho đẹp
          String catLabel = type;
          if (type.equalsIgnoreCase("VEHICLE")) catLabel = "Vehicle";
          else if (type.equalsIgnoreCase("ART")) catLabel = "Art";
          else if (type.equalsIgnoreCase("ELECTRONICS")) catLabel = "Electronics";

          data.add(new AuctionRow(id, name, catLabel, seller, price, status, reason));
        }
        return data;
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      if (listingsTable != null) listingsTable.setItems(task.getValue());
    }));
    task.setOnFailed(e -> task.getException().printStackTrace());
    new Thread(task).start();
  }

  // Approve / Reject actions 

  private void approveAuction(int auctionId) {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        ApiClient.post("/auction/approve", Map.of("auctionId", auctionId));
        return null;
      }
    };
    task.setOnSucceeded(e -> { loadTable("PENDING"); loadStats(); });
    task.setOnFailed(e -> task.getException().printStackTrace());
    new Thread(task).start();
  }

  private void rejectAuction(int auctionId) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Reject Auction");
    dialog.setHeaderText("Auction ID: " + auctionId);
    dialog.setContentText("Reject reason:");

    dialog.showAndWait().ifPresent(reason -> {
      if (reason.isBlank()) return;
      Task<Void> task = new Task<>() {
        @Override
        protected Void call() throws Exception {
          ApiClient.post("/auction/reject", Map.of("auctionId", auctionId, "reason", reason.trim()));
          return null;
        }
      };
      task.setOnSucceeded(e -> { loadTable("PENDING"); loadStats(); });
      task.setOnFailed(e -> task.getException().printStackTrace());
      new Thread(task).start();
    });
  }

  // Users data 

  private void loadUsersTable(String filter) {
    Task<ObservableList<UserRow>> task = new Task<>() {
      @Override
      protected ObservableList<UserRow> call() throws Exception {
        ObservableList<UserRow> data = FXCollections.observableArrayList();
        JsonArray arr = ApiClient.getArray("/admin/users?filter=" + filter);
        for (JsonElement el : arr) {
          JsonObject obj = el.getAsJsonObject();
          int    userId   = obj.has("id") ? obj.get("id").getAsInt() : 0;
          String username = !obj.get("username").isJsonNull() ? obj.get("username").getAsString() : "";
          String email    = !obj.get("email").isJsonNull()    ? obj.get("email").getAsString()    : "";
          String role     = !obj.get("role").isJsonNull()     ? obj.get("role").getAsString()     : "";
          boolean active  = "Active".equals(obj.get("status").getAsString());
          String joinDate = !obj.get("joinDate").isJsonNull() ? obj.get("joinDate").getAsString() : "N/A";
          data.add(new UserRow(userId, username, email, role, active, joinDate));
        }
        return data;
      }
    };
    task.setOnSucceeded(e -> Platform.runLater(() -> {
      if (usersTable != null) usersTable.setItems(task.getValue());
    }));
    task.setOnFailed(e -> task.getException().printStackTrace());
    new Thread(task).start();
  }

  private void banUser(int userId) {
    setUserActive(userId, false);
  }

  private void unbanUser(int userId) {
    setUserActive(userId, true);
  }

  private void setUserActive(int userId, boolean active) {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        ApiClient.post("/admin/user/ban", Map.of("userId", userId, "active", active));
        return null;
      }
    };
    task.setOnSucceeded(e -> { loadUsersTable("ALL"); loadStats(); });
    task.setOnFailed(e -> task.getException().printStackTrace());
    new Thread(task).start();
  }
}
