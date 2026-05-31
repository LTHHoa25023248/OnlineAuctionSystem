package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.client.ApiClient;
import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.service.ImageStorageService;
import com.example.auctionmanagementsystem.observer.Observer;
import com.example.auctionmanagementsystem.observer.AuctionNotifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// IMPLEMENTS OBSERVER 
public class AuctionListController implements Observer {

  // FXML fields — Top bar
  @FXML private Label usernameLabel;
  @FXML private MFXButton logoutButton;
  @FXML private MFXButton themeButton;
  @FXML private MFXButton notificationButton;
  @FXML private Label notificationBadge;

  // FXML fields — Sidebar 
  @FXML private HBox homeButton;
  @FXML private HBox activeListingButton;
  @FXML private HBox yourListingButton;
  @FXML private HBox profileButton;
  @FXML private MFXButton sellButton;
  @FXML private MFXButton adminButton;

  // FXML fields — Category filter 
  @FXML private Label allCat;
  @FXML private Label electronicsCat;
  @FXML private Label artCat;
  @FXML private Label vehicleCat;

  // FXML fields — Content 
  @FXML private MFXTextField searchNameField;
  @FXML private MFXButton searchNameButton;
  @FXML private MFXButton sortButton;
  @FXML private MFXScrollPane scrollPane;

  // ── FXML fields — Notification (Bạn có thể thêm nó vào file FXML của mình)
  @FXML private ListView<String> notificationList;

  // State 
  private String currentCategory = "ALL";
  private String currentSort = "NEWEST";
  private String currentSearchName = "";

  // Track auction IDs đã thông báo trong session này, tránh hiện lại mỗi lần reload
  private static final Set<Integer> notifiedWins = new HashSet<>();

  // Model 
  public static class AuctionItem {
    public int id;
    public int itemId;
    public String name;
    public String category;
    public String imagePath;
    public double price;
    public int bids;
    public int daysLeft;
    public String status;   // PENDING / OPEN / RUNNING / FINISHED ...
    public int sellerId;
    public java.time.LocalDateTime endTime;
    private Map<String, String> attributes;


    public Map<String, String> getAttributes() {
      return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
    }

    public String getAttribute(String key) {
      if (attributes != null && attributes.containsKey(key)) {
        return attributes.get(key);
      }
      return ""; // Trả về chuỗi rỗng nếu không có thuộc tính đó
    }

    public AuctionItem(int id, String name, String category, double price,
                       int bids, int daysLeft, String imagePath) {
      this(id, name, category, price, bids, daysLeft, imagePath, "RUNNING", 0);
    }

    public AuctionItem(int id, String name, String category, double price,
                       int bids, int daysLeft, String imagePath,
                       String status, int sellerId) {
      this.id = id;
      this.name = name;
      this.category = category;
      this.price = price;
      this.bids = bids;
      this.daysLeft = daysLeft;
      this.imagePath = imagePath;
      this.status = status != null ? status : "RUNNING";
      this.sellerId = sellerId;
    }
  }

  @FXML
  public void initialize() {
    usernameLabel.setText(SessionManager.getInstance().getUsername());

    // Wire sidebar 
    homeButton.setOnMouseClicked(e -> { currentCategory = "ALL";    loadListings(); });
    activeListingButton.setOnMouseClicked(e -> { currentCategory = "ACTIVE"; loadListings(); });
    yourListingButton.setOnMouseClicked(e -> { currentCategory = "MINE";   loadListings(); });
    profileButton.setOnMouseClicked(e -> openProfile());

    // Wire buttons 
    sellButton.setOnAction(e -> onSellButtonClick());
    sortButton.setOnAction(e -> onSortByButtonClick());
    logoutButton.setOnAction(e -> onLogOutButtonClick());

    // Notification bell + badge 
    NotificationStore store = NotificationStore.getInstance();
    if (notificationButton != null) {
      notificationButton.setOnAction(e -> onNotificationButtonClick());
    }
    if (notificationBadge != null) {
      // Badge hiện số chưa đọc; ẩn hẳn khi = 0
      notificationBadge.textProperty().bind(store.unreadCountProperty().asString());
      notificationBadge.visibleProperty().bind(store.unreadCountProperty().greaterThan(0));
      notificationBadge.managedProperty().bind(notificationBadge.visibleProperty());
    }

    // Theme toggle 
    if (themeButton != null) {
      updateThemeButtonText();
      themeButton.setOnAction(e -> {
        ThemeManager.getInstance().toggleTheme(themeButton.getScene());
        updateThemeButtonText();
      });
    }

    // Sell button — chỉ hiện với Seller 
    if (sellButton != null) {
      boolean isSeller = SessionManager.getInstance().isSeller();
      sellButton.setVisible(isSeller);
      sellButton.setManaged(isSeller);
    }

    // Admin button — chỉ hiện với Admin 
    if (adminButton != null) {
      boolean isAdmin = SessionManager.getInstance().isAdmin();
      adminButton.setVisible(isAdmin);
      adminButton.setManaged(isAdmin);
      adminButton.setOnAction(e -> onAdminButtonClick());
    }

    // Category labels 
    Label[] cats     = {allCat, electronicsCat, artCat, vehicleCat};
    String[] filters = {"ALL", "Electronics", "Art", "Vehicle"};
    for (int i = 0; i < cats.length; i++) {
      final String f = filters[i];
      final Label  c = cats[i];
      if (c != null)
        c.setOnMouseClicked(e -> { selectCategory(cats, c); currentCategory = f; loadListings(); });
    }

    // ĐĂNG KÝ VÀO HỆ THỐNG TRUYỀN TIN RAM (OBSERVER) 
    AuctionNotifier.getInstance().registerObserver(this);

    // Đảm bảo hủy đăng ký lắng nghe khi tắt cửa sổ chính để giải phóng bộ nhớ
    if (scrollPane != null) {
      scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
        if (newScene != null) {
          newScene.windowProperty().addListener((wObs, oldWindow, newWindow) -> {
            if (newWindow != null) {
              ((Stage) newWindow).setOnCloseRequest(windowEvent -> {
                AuctionNotifier.getInstance().removeObserver(this);
              });
            }
          });
        }
      });
    }

    loadListings();
    if (SessionManager.getInstance().isBidder()) checkWinnerNotifications();
  }

  // TRIỂN KHAI PHƯƠNG THỨC UPDATE ĐỂ NHẬN TIN NHẮN REALTIME (KẾT QUẢ ĐẤU GIÁ)
  @Override
  public void update(String message) {
    if (message == null) return;

    String[] parts = message.split("\\|");
    if (parts.length < 2) return;

    String eventType = parts[0];

    // Xử lý thông báo kết thúc phiên đấu giá
    if ("AUCTION_RESULT".equals(eventType) && parts.length >= 5) {
      try {
        int auctionId = Integer.parseInt(parts[1]);
        String itemName = safeName(parts[2], auctionId);
        int winnerId = Integer.parseInt(parts[3]);
        double finalPrice = Double.parseDouble(parts[4]);

        // Lấy ID người dùng đang đăng nhập
        int currentUserId = SessionManager.getInstance().getUserId();

        Platform.runLater(() -> {
          String notifMsg = "";
          boolean isWinner = false;

          if (winnerId <= 0) {
            notifMsg = "🔔 Auction '" + itemName + "' ended (No buyer).";
          } else if (currentUserId == winnerId) {
            notifMsg = "🎉 YOU WON! Successfully purchased '" + itemName + "' (Price: $" + finalPrice + ")";
            isWinner = true;
          } else {
            notifMsg = "🔔 Auction '" + itemName + "' ended (Sold to another bidder).";
          }

          // 1. Đẩy vào kho thông báo dùng chung -> hiện trong panel chuông + tăng badge
          NotificationStore.getInstance().addMessage(notifMsg);

          // (giữ tương thích nếu sau này FXML có gắn ListView trực tiếp)
          if (notificationList != null) {
            notificationList.getItems().add(0, notifMsg);
          }

          // 2. Hiện Popup mạnh cho người chiến thắng
          if (isWinner && !notifiedWins.contains(auctionId)) {
            notifiedWins.add(auctionId); // Đánh dấu để không hiện lại popup nhiều lần
            // Reload danh sách trước để cập nhật nền phía sau dialog modal
            loadListings();
            javafx.stage.Window owner = (scrollPane != null && scrollPane.getScene() != null)
                    ? scrollPane.getScene().getWindow() : null;
            ResultDialog.showWin(owner, itemName, finalPrice, -1);
            return;
          }

          // Reload lại danh sách bên ngoài sau khi có biến động
          loadListings();
        });
      } catch (Exception e) {
        System.err.println("Lỗi xử lý AUCTION_RESULT: " + e.getMessage());
      }
    }
  }

  /** Trả về tên hiển thị an toàn: bỏ null/blank/literal "null", thay bằng "Auction #id". */
  private static String safeName(String raw, int auctionId) {
    return (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw))
            ? ("Auction #" + auctionId) : raw;
  }


  private void checkWinnerNotifications() {
    int userId = SessionManager.getInstance().getUserId();
    Task<List<ResultDialog.WinItem>> task = new Task<>() {
      @Override
      protected List<ResultDialog.WinItem> call() throws Exception {
        List<ResultDialog.WinItem> wins = new ArrayList<>();
        JsonArray data = ApiClient.getArray("/user/wins?userId=" + userId);
        for (JsonElement el : data) {
          JsonObject obj = el.getAsJsonObject();
          int id = obj.get("auctionId").getAsInt();
          if (!notifiedWins.contains(id)) {
            notifiedWins.add(id);
            String itemName = obj.get("itemName").getAsString();
            double price = obj.get("price").getAsDouble();
            wins.add(new ResultDialog.WinItem(
                    String.format("Auction #%d — %s", id, itemName), price));
            // Lưu vào kho thông báo để xem lại trong panel chuông
            NotificationStore.getInstance().addMessage(
                    String.format("🎉 Auction #%d — %s\nAmount paid: %,.2f USD", id, itemName, price));
          }
        }
        return wins;
      }
    };
    task.setOnSucceeded(e -> {
      List<ResultDialog.WinItem> wins = task.getValue();
      if (wins == null || wins.isEmpty()) return;
      Platform.runLater(() -> {
        javafx.stage.Window owner = (scrollPane != null && scrollPane.getScene() != null)
                ? scrollPane.getScene().getWindow() : null;
        if (wins.size() == 1) {
          ResultDialog.WinItem w = wins.get(0);
          ResultDialog.showWin(owner, w.title, w.amount, -1);
        } else {
          ResultDialog.showWinSummary(owner, wins);
        }
      });
    });
    new Thread(task).start();
  }

  private void updateThemeButtonText() {
    if (themeButton == null) return;
    themeButton.setText(ThemeManager.getInstance().isDarkMode() ? "Light" : "Dark");
  }

  // Action handlers 

  @FXML
  private void onLogOutButtonClick() {
    // THAY ĐỔI: Phải hủy lắng nghe khi Logout để tránh lỗi hiển thị nhầm tài khoản sau này
    AuctionNotifier.getInstance().removeObserver(this);

    SessionManager.getInstance().logout();
    NavigationUtil.goTo(logoutButton, NavigationUtil.LOGIN);
  }

  @FXML
  private void onSellButtonClick() {
    AddListingController.lastAddedItem = null;
    NavigationUtil.openPopup(sellButton, NavigationUtil.ADD_LISTING, "Add Listing");
    // Reload từ DB để hiện item vừa thêm
    loadListings();
  }

  @FXML
  private void onSortByButtonClick() {
    SortingMenuController ctrl =
            NavigationUtil.openPopup(sortButton, NavigationUtil.SORTING_MENU, "Sort By");
    if (ctrl != null) {
      currentSort = ctrl.getSelectedSort();
      loadListings();
    }
  }

  @FXML
  private void onAdminButtonClick() {
    NavigationUtil.goTo(adminButton, NavigationUtil.ADMIN);
  }

  private void openProfile() {
    NavigationUtil.openPopup(profileButton, NavigationUtil.PROFILE, "My Profile");
  }

  @FXML
  private void onNotificationButtonClick() {
    // Mở panel = đã xem -> tắt badge (controller của panel cũng gọi markAllRead)
    NotificationStore.getInstance().markAllRead();
    NavigationUtil.openPopup(notificationButton, NavigationUtil.NOTIFICATIONS, "Notifications");
  }

  public void refresh() { loadListings(); }

  @FXML
  private void onSearchByName() {
    String text = searchNameField != null ? searchNameField.getText().trim() : "";
    currentSearchName = text;
    loadListings();
  }

  public void openAuctionDetail(int auctionId) {
    NavigationUtil.openPopupWith(scrollPane, NavigationUtil.AUCTION_DETAIL, "Auction Detail",
            (AuctionDetailController ctrl) -> ctrl.loadAuction(auctionId));
  }

  // Data — load từ DB 

  /**
   * JOIN auction + items + bid_transaction + electronics_items (để lấy brand làm category).
   * Chạy trên background thread để không block UI.
   */
  private void loadListings() {
    Task<List<AuctionItem>> task = new Task<>() {
      @Override
      protected List<AuctionItem> call() {
        return fetchFromDB();
      }
    };

    task.setOnSucceeded(e -> {
      List<AuctionItem> items = task.getValue();
      System.out.println("[AuctionList] Task succeeded, items=" + items.size() + ", category=" + currentCategory);
      renderGrid(filterAndSort(items));
    });
    task.setOnFailed(e -> {
      task.getException().printStackTrace();
      renderGrid(new ArrayList<>()); // hiện lưới trống nếu lỗi DB
    });

    new Thread(task).start();
  }

  private List<AuctionItem> fetchFromDB() {
    List<AuctionItem> result = new ArrayList<>();
    try {
      JsonArray data = ApiClient.getArray("/auction/list");
      for (JsonElement el : data) {
        JsonObject obj = el.getAsJsonObject();
        int    id       = obj.get("id").getAsInt();
        int    itemId   = obj.get("itemId").getAsInt();
        String name     = obj.get("name").isJsonNull() ? "Unknown" : obj.get("name").getAsString();
        String category = obj.get("category").getAsString();
        double price    = obj.get("price").getAsDouble();
        int    bids     = obj.get("bids").getAsInt();
        int    sellerId = obj.get("sellerId").getAsInt();
        String status   = obj.get("status").isJsonNull() ? "RUNNING" : obj.get("status").getAsString();

        int daysLeft = 0;
        LocalDateTime endTime = null;
        if (!obj.get("endTime").isJsonNull()) {
          endTime  = LocalDateTime.parse(obj.get("endTime").getAsString());
          daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), endTime.toLocalDate());
          daysLeft = Math.max(0, daysLeft);
        }

        String fileName  = obj.get("imagePath").isJsonNull() ? null : obj.get("imagePath").getAsString();
        String imagePath = (fileName != null && !fileName.isBlank())
                ? ImageStorageService.getFullPath(fileName) : null;

        AuctionItem ai = new AuctionItem(id, name, category, price, bids, daysLeft,
                imagePath, status, sellerId);
        ai.itemId  = itemId;
        ai.endTime = endTime;
        // --- BẮT ĐẦU THÊM: XỬ LÝ ATTRIBUTES ĐỘNG ---
        if (obj.has("attributes") && !obj.get("attributes").isJsonNull()) {
          JsonObject attrJson = obj.getAsJsonObject("attributes");
          Map<String, String> attributesMap = new java.util.HashMap<>();

          // Duyệt qua tất cả các key (như brand, warranty, v.v.) trong cục attributes
          for (java.util.Map.Entry<String, JsonElement> entry : attrJson.entrySet()) {
            // Đảm bảo không dính null khi getAsString()
            if (!entry.getValue().isJsonNull()) {
              attributesMap.put(entry.getKey(), entry.getValue().getAsString());
            }
          }

          // Gán Map này vào đối tượng AuctionItem của bạn
          ai.setAttributes(attributesMap);
        }
        // --- KẾT THÚC THÊM ---
        result.add(ai);
      }
      System.out.println("[AuctionList] fetchFromDB returned " + result.size() + " items");
    } catch (Exception e) {
      System.err.println("[AuctionList] fetchFromDB ERROR: " + e.getMessage());
      e.printStackTrace();
    }
    return result;
  }

  private String resolveCategory(String itemType, String brand) {
    if ("VEHICLE".equals(itemType))     return "Vehicle";
    if ("ART".equals(itemType))         return "Art";
    return "Electronics";
  }

  private List<AuctionItem> filterAndSort(List<AuctionItem> list) {
    int myId = SessionManager.getInstance().getUserId();

    // Lọc theo tên (case-insensitive substring) nếu có từ khóa tìm
    if (currentSearchName != null && !currentSearchName.isBlank()) {
      String needle = currentSearchName.toLowerCase();
      list.removeIf(item -> item.name == null || !item.name.toLowerCase().contains(needle));
    }

    list.removeIf(item -> {
      return switch (currentCategory) {
        case "ALL"      -> false;
        case "ACTIVE"   -> !"RUNNING".equals(item.status) && !"OPEN".equals(item.status);
        case "TRENDING" -> false; // hiện tất cả, sort theo bids bên dưới
        case "MINE"     -> item.sellerId != myId;
        default         -> !item.category.equalsIgnoreCase(currentCategory);
      };
    });

    switch (currentSort) {
      case "PRICE_DESC"  -> list.sort(Comparator.comparingDouble((AuctionItem a) -> a.price).reversed());
      case "PRICE_ASC"   -> list.sort(Comparator.comparingDouble(a -> a.price));
      case "NEWEST"      -> list.sort(Comparator.comparingInt((AuctionItem a) -> a.id).reversed());
      case "OLDEST"      -> list.sort(Comparator.comparingInt(a -> a.id));
      case "ENDING_SOON" -> list.sort(Comparator.comparingInt(a -> a.daysLeft));
      default            -> {
        if ("TRENDING".equals(currentCategory))
          list.sort(Comparator.comparingInt((AuctionItem a) -> a.bids).reversed());
        else
          list.sort(Comparator.comparingInt((AuctionItem a) -> a.id).reversed());
      }
    }
    return list;
  }

  private void renderGrid(List<AuctionItem> list) {
    Platform.runLater(() -> {
      try {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(16));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        int index = 0;
        for (AuctionItem item : list) {
          try {
            java.net.URL cardUrl = NavigationUtil.class.getResource("../" + NavigationUtil.AUCTION_CARD);
            if (cardUrl == null)
              cardUrl = NavigationUtil.class.getResource("/com/example/auctionmanagementsystem/" + NavigationUtil.AUCTION_CARD);
            if (cardUrl == null) {
              System.err.println("[AuctionList] FXML not found: " + NavigationUtil.AUCTION_CARD);
              continue;
            }
            FXMLLoader loader = new FXMLLoader(cardUrl);
            Node card = loader.load();
            AuctionCardController ctrl = loader.getController();
            ctrl.setAuction(item, this);
            ctrl.setImage(item.imagePath);
            GridPane.setHgrow(card, Priority.ALWAYS);
            grid.add(card, index % 2, index / 2);
            index++;
          } catch (Exception e) {
            System.err.println("=== CARD LOAD FAILED ===");
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message:   " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
              System.err.println("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
              cause = cause.getCause();
            }
            System.err.println("========================");
          }
        }
        System.out.println("[AuctionList] renderGrid: added " + index + "/" + list.size() + " cards");
        if (scrollPane != null) {
          scrollPane.setContent(grid);
        } else {
          System.err.println("[AuctionList] ERROR: scrollPane is NULL");
        }
      } catch (Exception outer) {
        System.err.println("[AuctionList] renderGrid OUTER crash: " + outer.getClass().getSimpleName() + ": " + outer.getMessage());
        outer.printStackTrace();
      }
    });
  }

  private void selectCategory(Label[] all, Label selected) {
    for (Label l : all) {
      if (l != null) l.getStyleClass().remove("catSelected");
    }
    if (selected != null && !selected.getStyleClass().contains("catSelected"))
      selected.getStyleClass().add("catSelected");
  }
}
