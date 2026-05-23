package com.example.auctionmanagementsystem.controller;

import com.example.auctionmanagementsystem.model.Seller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AuctionListController {

  // ── FXML fields — Top bar ─────────────────────────────────────────────────
  @FXML
  private Label usernameLabel;
  @FXML
  private MFXButton logoutButton;
  @FXML
  private MFXButton themeButton;

  // ── FXML fields — Sidebar ─────────────────────────────────────────────────
  @FXML
  private HBox homeButton;
  @FXML
  private HBox activeListingButton;
  @FXML
  private HBox trendingButton;
  @FXML
  private HBox yourListingButton;
  @FXML
  private HBox watchListButton;
  @FXML
  private HBox profileButton;
  @FXML
  private MFXButton sellButton;
  @FXML
  private MFXButton adminButton;

  // ── FXML fields — Category filter ────────────────────────────────────────
  @FXML
  private Label allCat;
  @FXML
  private Label jewelryCat;
  @FXML
  private Label watchesCat;
  @FXML
  private Label bagsCat;
  @FXML
  private Label fineArtCat;
  @FXML
  private Label carsCat;
  @FXML
  private Label othersCat;

  // ── FXML fields — Content ────────────────────────────────────────────────
  @FXML
  private MFXButton sortButton;
  @FXML
  private MFXScrollPane scrollPane;

  // ── State ─────────────────────────────────────────────────────────────────
  private String currentCategory = "ALL";
  private String currentSort = "NEWEST";
  private final List<AuctionItem> userAddedItems = new ArrayList<>();

  // ── Model ─────────────────────────────────────────────────────────────────
  public static class AuctionItem {
    public int id;
    public String name;
    public String category;
    public String imagePath;
    public double price;
    public int bids;
    public int daysLeft;

    public AuctionItem(int id, String name, String category, double price, int bids, int daysLeft,
        String imagePath) {
      this.id = id;
      this.name = name;
      this.category = category;
      this.price = price;
      this.bids = bids;
      this.daysLeft = daysLeft;
      this.imagePath = imagePath;
    }
  }

  @FXML
  public void initialize() {
    usernameLabel.setText(SessionManager.getInstance().getUsername());

    // ── Wire sidebar ──────────────────────────────────────────────────────
    homeButton.setOnMouseClicked(e -> {
      currentCategory = "ALL";
      loadListings();
    });
    activeListingButton.setOnMouseClicked(e -> {
      currentCategory = "ACTIVE";
      loadListings();
    });
    trendingButton.setOnMouseClicked(e -> {
      currentCategory = "TRENDING";
      loadListings();
    });
    yourListingButton.setOnMouseClicked(e -> {
      currentCategory = "MINE";
      loadListings();
    });
    watchListButton.setOnMouseClicked(e -> {
      currentCategory = "WATCHLIST";
      loadListings();
    });
    profileButton.setOnMouseClicked(e -> openProfile());

    // ── Wire buttons ──────────────────────────────────────────────────────
    sellButton.setOnAction(e -> onSellButtonClick());
    sortButton.setOnAction(e -> onSortByButtonClick());
    logoutButton.setOnAction(e -> onLogOutButtonClick());

    // ── Theme toggle ──────────────────────────────────────────────────────
    if (themeButton != null) {
      updateThemeButtonText();
      themeButton.setOnAction(e -> {
        ThemeManager.getInstance().toggleTheme(themeButton.getScene());
        updateThemeButtonText();
      });
    }

    // ── Sell button — chỉ hiện với Seller ────────────────────────────────
    if (sellButton != null) {
      boolean isSeller = SessionManager.getInstance().isSeller();
      sellButton.setVisible(isSeller);
      sellButton.setManaged(isSeller);
    }

    // ── Admin button — chỉ hiện với Admin ────────────────────────────────
    if (adminButton != null) {
      boolean isAdmin = SessionManager.getInstance().isAdmin();
      adminButton.setVisible(isAdmin);
      adminButton.setManaged(isAdmin);
      adminButton.setOnAction(e -> onAdminButtonClick());
    }

    // ── Category labels ───────────────────────────────────────────────────
    Label[] cats = {allCat, jewelryCat, watchesCat, bagsCat, fineArtCat, carsCat, othersCat};
    String[] filters = {"ALL", "Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others"};
    for (int i = 0; i < cats.length; i++) {
      final String f = filters[i];
      final Label c = cats[i];
      if (c != null)
        c.setOnMouseClicked(e -> {
          selectCategory(cats, c);
          currentCategory = f;
          loadListings();
        });
    }

    loadListings();
  }

  private void updateThemeButtonText() {
    if (themeButton == null)
      return;
    if (ThemeManager.getInstance().isDarkMode()) {
      themeButton.setText("Light");
    } else {
      themeButton.setText("Dark");
    }
  }

  // ── Action handlers ───────────────────────────────────────────────────────

  @FXML
  private void onLogOutButtonClick() {
    SessionManager.getInstance().logout();
    NavigationUtil.goTo(logoutButton, NavigationUtil.LOGIN);
  }

  @FXML
  private void onSellButtonClick() {
    AddListingController.lastAddedItem = null;
    NavigationUtil.openPopup(sellButton, NavigationUtil.ADD_LISTING, "Add Listing");
    AuctionItem newItem = AddListingController.lastAddedItem;
    if (newItem != null) {
      userAddedItems.add(0, newItem);
    }
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

  public void openAuctionDetail(int auctionId) {
    AuctionDetailController ctrl =
        NavigationUtil.openPopup(scrollPane, NavigationUtil.AUCTION_DETAIL, "Auction Detail");
    if (ctrl != null)
      ctrl.loadAuction(auctionId);
  }

  // ── Data ──────────────────────────────────────────────────────────────────

  private List<AuctionItem> getAllItems() {
    List<AuctionItem> list = new ArrayList<>();
    list.addAll(userAddedItems);

    String[] categories = {"Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others"};
    String[][] names = {
        {"Rolex Daytona", "Diamond Ring", "Pearl Necklace", "Gold Bracelet", "Ruby Earrings",
            "Sapphire Pendant", "Emerald Ring", "Platinum Chain"},
        {"Patek Philippe", "Omega Seamaster", "Audemars Piguet", "TAG Heuer Monaco",
            "IWC Portugieser", "Cartier Santos", "Hublot Big Bang", "Richard Mille"},
        {"Hermes Birkin", "Louis Vuitton Neverfull", "Chanel Flap", "Gucci Dionysus",
            "Prada Galleria", "Dior Lady", "Bottega Veneta", "Balenciaga City"},
        {"Mona Lisa Print", "Starry Night", "The Scream", "Water Lilies", "Girl with Pearl",
            "The Birth of Venus", "Guernica Print", "Sunflowers"},
        {"Porsche 911", "Ferrari 488", "Lamborghini Huracan", "McLaren 720S", "Aston Martin DB11",
            "Rolls Royce Ghost", "Bentley Continental", "BMW M8"},
        {"Vintage Guitar", "Antique Clock", "Rare Coin", "Vintage Camera", "Antique Vase",
            "Rare Stamp", "Ancient Sword", "Rare Book"}};

    Random random = new Random(42);
    for (int id = 1; id <= 100; id++) {
      int catIdx = random.nextInt(6);
      String cat = categories[catIdx];
      String itemName = names[catIdx][random.nextInt(names[catIdx].length)];
      double price = (random.nextInt(990) + 10) * 100.0;
      int bids = random.nextInt(20);
      int daysLeft = random.nextInt(30) + 1;
      list.add(new AuctionItem(id, itemName, cat, price, bids, daysLeft, null));
    }
    return list;
  }

  private void loadListings() {
        List<AuctionItem> list = getAllItems();

        list.removeIf(item -> {
            boolean isSpecial = currentCategory.equals("ALL")
                    || currentCategory.equals("ACTIVE")
                    || currentCategory.equals("TRENDING")
                    || currentCategory.equals("MINE")
                    || currentCategory.equals("WATCHLIST");
            return !isSpecial && !item.category.equalsIgnoreCase(currentCategory);
        });

        switch (currentSort) {
            case "PRICE_DESC"  -> list.sort(Comparator.comparingDouble((AuctionItem a) -> a.price).reversed());
            case "PRICE_ASC"   -> list.sort(Comparator.comparingDouble(a -> a.price));
            case "NEWEST"      -> list.sort(Comparator.comparingInt((AuctionItem a) -> a.id).reversed());
            case "OLDEST"      -> list.sort(Comparator.comparingInt(a -> a.id));
            case "ENDING_SOON" -> list.sort(Comparator.comparingInt(a -> a.daysLeft));
            default            -> list.sort(Comparator.comparingInt((AuctionItem a) -> a.daysLeft).reversed());
        }

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
                FXMLLoader loader = new FXMLLoader(
                        NavigationUtil.class.getResource(
                                "../" + NavigationUtil.AUCTION_CARD));
                Node card = loader.load();
                AuctionCardController ctrl = loader.getController();
                ctrl.setAuction(item, this);
                ctrl.setImage(item.imagePath);
                GridPane.setHgrow(card, Priority.ALWAYS);
                grid.add(card, index % 2, index / 2);
                index++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        scrollPane.setContent(grid);
    }

  private void selectCategory(Label[] all, Label selected) {
    for (Label l : all) {
      if (l != null)
        l.getStyleClass().remove("catSelected");
    }
    if (!selected.getStyleClass().contains("catSelected"))
      selected.getStyleClass().add("catSelected");
  }
}
