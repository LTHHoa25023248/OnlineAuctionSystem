//package com.example.auctionmanagementsystem.controller;
//
//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.layout.HBox;
//import io.github.palexdev.materialfx.controls.MFXButton;
//import io.github.palexdev.materialfx.controls.MFXScrollPane;
//
///**
// * Controller cho View/auction_list.fxml
// *
// * Điều hướng:
// *   homeButton / activeListingButton / trendingButton → load grid
// *   yourListingButton → load listing của user hiện tại
// *   watchListButton   → load watchlist
// *   profileButton     → auction_profile.fxml (popup)
// *   sellButton        → add_listing.fxml (popup)
// *   sortButton        → sortingmenu.fxml (popup)
// *   logoutButton      → auction_login.fxml
// *   click card        → auction_detail.fxml (popup)
// */
//public class AuctionListController {
//
//    // ── Top bar ──────────────────────────────────────────────────────────────
//    @FXML private Label     usernameLabel;
//    @FXML private MFXButton logoutButton;
//
//    // ── Sidebar ──────────────────────────────────────────────────────────────
//    @FXML private HBox      homeButton;
//    @FXML private HBox      activeListingButton;
//    @FXML private HBox      trendingButton;
//    @FXML private HBox      yourListingButton;
//    @FXML private HBox      watchListButton;
//    @FXML private HBox      profileButton;
//    @FXML private MFXButton sellButton;
//
//    // ── Category bar ─────────────────────────────────────────────────────────
//    @FXML private Label allCat;
//    @FXML private Label jewelryCat;
//    @FXML private Label watchesCat;
//    @FXML private Label bagsCat;
//    @FXML private Label fineArtCat;
//    @FXML private Label carsCat;
//    @FXML private Label othersCat;
//
//    // ── Content ──────────────────────────────────────────────────────────────
//    @FXML private MFXButton     sortButton;
//    @FXML private MFXScrollPane scrollPane;
//
//    @FXML
//    public void initialize() {
//        usernameLabel.setText(SessionManager.getInstance().getUsername());
//
//        // Sidebar
//        homeButton.setOnMouseClicked(e          -> loadListings("ALL"));
//        activeListingButton.setOnMouseClicked(e -> loadListings("ACTIVE"));
//        trendingButton.setOnMouseClicked(e      -> loadListings("TRENDING"));
//        yourListingButton.setOnMouseClicked(e   -> loadListings("MINE"));
//        watchListButton.setOnMouseClicked(e     -> loadListings("WATCHLIST"));
//        profileButton.setOnMouseClicked(e       -> openProfile());
//
//        sellButton.setOnAction(e  -> onSellButtonClick());
//        sortButton.setOnAction(e  -> onSortByButtonClick());
//        logoutButton.setOnAction(e -> onLogOutButtonClick());
//
//        // Category labels
//        Label[] cats    = {allCat, jewelryCat, watchesCat, bagsCat, fineArtCat, carsCat, othersCat};
//        String[] filters = {"ALL","Jewelry","Watches","Bags","Fine Art","Cars","Others"};
//        for (int i = 0; i < cats.length; i++) {
//            final String f = filters[i];
//            final Label  c = cats[i];
//            if (c != null) c.setOnMouseClicked(e -> {
//                selectCategory(cats, c);
//                loadListings(f);
//            });
//        }
//
//        loadListings("ALL");
//    }
//
//    // ── Điều hướng ────────────────────────────────────────────────────────────
//
//    @FXML
//    private void onLogOutButtonClick() {
//        SessionManager.getInstance().logout();
//        NavigationUtil.goTo(logoutButton, NavigationUtil.LOGIN);
//    }
//
//    @FXML
//    private void onSellButtonClick() {
//        NavigationUtil.openPopup(sellButton, NavigationUtil.ADD_LISTING, "Thêm sản phẩm");
//        loadListings("ALL");
//    }
//
//    @FXML
//    private void onSortByButtonClick() {
//        SortingMenuController ctrl =
//                NavigationUtil.openPopup(sortButton, NavigationUtil.SORTING_MENU, "Sắp xếp");
//        if (ctrl != null) loadListings(ctrl.getSelectedSort());
//    }
//
//    private void openProfile() {
//        NavigationUtil.openPopup(profileButton, NavigationUtil.PROFILE, "Hồ sơ của tôi");
//    }
//
//    /** Gọi từ AuctionCardController khi user click vào card */
//    public void openAuctionDetail(int auctionId) {
//        AuctionDetailController ctrl =
//                NavigationUtil.openPopup(scrollPane, NavigationUtil.AUCTION_DETAIL, "Chi tiết đấu giá");
//        if (ctrl != null) ctrl.loadAuction(auctionId);
//    }
//
//    // ── Load data ─────────────────────────────────────────────────────────────
//
//    private void loadListings(String filter) {
//        /* ── Thay bằng DAO + load auction_card.fxml ─────────────────────────
//         *
//         *   List<Auction> list = AuctionDAO.getByFilter(filter,
//         *       SessionManager.getInstance().getUserId());
//         *
//         *   FlowPane grid = new FlowPane();
//         *   grid.setHgap(16); grid.setVgap(16);
//         *
//         *   for (Auction a : list) {
//         *       FXMLLoader loader = new FXMLLoader(
//         *           NavigationUtil.class.getResource("../" + NavigationUtil.AUCTION_CARD));
//         *       Node card = loader.load();
//         *       AuctionCardController ctrl = loader.getController();
//         *       ctrl.setAuction(a, this);
//         *       grid.getChildren().add(card);
//         *   }
//         *   scrollPane.setContent(grid);
//         *
//         * ─────────────────────────────────────────────────────────────────── */
//        System.out.println("Load listings: " + filter);
//    }
//
//    // ── Helper ────────────────────────────────────────────────────────────────
//
//    private void selectCategory(Label[] all, Label selected) {
//        for (Label l : all) {
//            if (l != null) l.getStyleClass().remove("catSelected");
//        }
//        if (!selected.getStyleClass().contains("catSelected"))
//            selected.getStyleClass().add("catSelected");
//    }
//}


// Đây là phần test để hiển thị phần sản phẩm,comment,sort,phân loại sp nhé
//
package com.example.auctionmanagementsystem.controller;

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

    @FXML private Label     usernameLabel;
    @FXML private MFXButton logoutButton;

    @FXML private HBox      homeButton;
    @FXML private HBox      activeListingButton;
    @FXML private HBox      trendingButton;
    @FXML private HBox      yourListingButton;
    @FXML private HBox      watchListButton;
    @FXML private HBox      profileButton;
    @FXML private MFXButton sellButton;
    @FXML private MFXButton adminButton;

    @FXML private Label allCat;
    @FXML private Label jewelryCat;
    @FXML private Label watchesCat;
    @FXML private Label bagsCat;
    @FXML private Label fineArtCat;
    @FXML private Label carsCat;
    @FXML private Label othersCat;

    @FXML private MFXButton     sortButton;
    @FXML private MFXScrollPane scrollPane;

    private String currentCategory = "ALL";
    private String currentSort     = "NEWEST";

    @FXML
    public void initialize() {
        usernameLabel.setText(SessionManager.getInstance().getUsername());

        homeButton.setOnMouseClicked(e          -> { currentCategory = "ALL";       loadListings(); });
        activeListingButton.setOnMouseClicked(e -> { currentCategory = "ACTIVE";    loadListings(); });
        trendingButton.setOnMouseClicked(e      -> { currentCategory = "TRENDING";  loadListings(); });
        yourListingButton.setOnMouseClicked(e   -> { currentCategory = "MINE";      loadListings(); });
        watchListButton.setOnMouseClicked(e     -> { currentCategory = "WATCHLIST"; loadListings(); });
        profileButton.setOnMouseClicked(e       -> openProfile());

        sellButton.setOnAction(e   -> onSellButtonClick());
        sortButton.setOnAction(e   -> onSortByButtonClick());
        logoutButton.setOnAction(e -> onLogOutButtonClick());

        if (adminButton != null) {
            boolean isAdmin = SessionManager.getInstance().isAdmin();
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
            adminButton.setOnAction(e -> onAdminButtonClick());
        }

        Label[] cats     = {allCat, jewelryCat, watchesCat, bagsCat, fineArtCat, carsCat, othersCat};
        String[] filters = {"ALL", "Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others"};
        for (int i = 0; i < cats.length; i++) {
            final String f = filters[i];
            final Label  c = cats[i];
            if (c != null) c.setOnMouseClicked(e -> {
                selectCategory(cats, c);
                currentCategory = f;
                loadListings();
            });
        }

        loadListings();
    }

    @FXML
    private void onLogOutButtonClick() {
        SessionManager.getInstance().logout();
        NavigationUtil.goTo(logoutButton, NavigationUtil.LOGIN);
    }

    @FXML
    private void onSellButtonClick() {
        NavigationUtil.openPopup(sellButton, NavigationUtil.ADD_LISTING, "Thêm sản phẩm");
        loadListings();
    }

    @FXML
    private void onSortByButtonClick() {
        SortingMenuController ctrl =
                NavigationUtil.openPopup(sortButton, NavigationUtil.SORTING_MENU, "Sắp xếp");
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
        NavigationUtil.openPopup(profileButton, NavigationUtil.PROFILE, "Hồ sơ của tôi");
    }

    public void openAuctionDetail(int auctionId) {
        AuctionDetailController ctrl =
                NavigationUtil.openPopup(scrollPane, NavigationUtil.AUCTION_DETAIL, "Chi tiết đấu giá");
        if (ctrl != null) ctrl.loadAuction(auctionId);
    }

    // ── Data mẫu ─────────────────────────────────────────────────────────────

    private static class AuctionItem {
        int id; String name; String category; double price; int bids; int daysLeft;
        AuctionItem(int id, String name, String category, double price, int bids, int daysLeft) {
            this.id = id; this.name = name; this.category = category;
            this.price = price; this.bids = bids; this.daysLeft = daysLeft;
        }
    }

    private List<AuctionItem> getSampleData() {
        List<AuctionItem> list = new ArrayList<>();

        String[] categories = {"Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others"};
        String[][] names = {
                {"Rolex Daytona", "Diamond Ring", "Pearl Necklace", "Gold Bracelet", "Ruby Earrings",
                        "Sapphire Pendant", "Emerald Ring", "Platinum Chain", "Diamond Brooch", "Amethyst Ring",
                        "Topaz Necklace", "Opal Earrings", "Garnet Bracelet", "Jade Pendant", "Coral Ring",
                        "Turquoise Necklace", "Onyx Bracelet", "Amber Ring"},
                {"Patek Philippe", "Chanel Watch", "Omega Seamaster", "Audemars Piguet", "Breitling Navitimer",
                        "TAG Heuer Monaco", "IWC Portugieser", "Vacheron Constantin", "Jaeger-LeCoultre", "Cartier Santos",
                        "Hublot Big Bang", "Richard Mille", "Panerai Luminor", "Longines Heritage", "Tissot Le Locle"},
                {"Hermes Birkin", "Louis Vuitton Neverfull", "Chanel Flap", "Gucci Dionysus", "Prada Galleria",
                        "Dior Lady", "Bottega Veneta", "Balenciaga City", "Fendi Baguette", "Givenchy Antigona",
                        "Celine Luggage", "Saint Laurent Sac", "Burberry Banner", "Coach Rogue", "Michael Kors Selma"},
                {"Mona Lisa Print", "Starry Night", "The Scream", "Water Lilies", "Girl with Pearl",
                        "The Birth of Venus", "American Gothic", "The Persistence", "Whistlers Mother", "The Kiss",
                        "Guernica Print", "Sunflowers", "The Thinker", "David Sculpture", "Venus de Milo",
                        "The Last Supper", "Creation of Adam", "Sistine Chapel"},
                {"Porsche 911", "Ferrari 488", "Lamborghini Huracan", "McLaren 720S", "Bugatti Chiron",
                        "Aston Martin DB11", "Rolls Royce Ghost", "Bentley Continental", "Mercedes SLS", "BMW M8",
                        "Audi R8", "Jaguar F-Type", "Maserati GranTurismo", "Alfa Romeo 4C", "Lotus Evija"},
                {"Vintage Guitar", "Antique Clock", "Rare Coin", "Vintage Camera", "Old Map",
                        "Antique Vase", "Vintage Poster", "Rare Stamp", "Ancient Sword", "Vintage Wine",
                        "Old Manuscript", "Rare Book", "Vintage Toy", "Antique Mirror", "Vintage Radio",
                        "Ancient Artifact", "Rare Fossil", "Vintage Typewriter"}
        };

        Random random = new Random(42);
        for (int id = 1; id <= 100; id++) {
            int    catIdx   = random.nextInt(6);
            String cat      = categories[catIdx];
            String name     = names[catIdx][random.nextInt(names[catIdx].length)];
            double price    = (random.nextInt(990) + 10) * 100.0;
            int    bids     = random.nextInt(20);
            int    daysLeft = random.nextInt(30) + 1;
            list.add(new AuctionItem(id, name, cat, price, bids, daysLeft));
        }

        return list;
    }

    // ── Load + filter + sort ──────────────────────────────────────────────────

    private void loadListings() {
        List<AuctionItem> list = getSampleData();

        list.removeIf(item -> {
            boolean special = currentCategory.equals("ALL")
                    || currentCategory.equals("ACTIVE")
                    || currentCategory.equals("TRENDING")
                    || currentCategory.equals("MINE")
                    || currentCategory.equals("WATCHLIST");
            return !special && !item.category.equalsIgnoreCase(currentCategory);
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
                        NavigationUtil.class.getResource("../" + NavigationUtil.AUCTION_CARD));
                Node card = loader.load();
                AuctionCardController ctrl = loader.getController();
                ctrl.setAuction(item.id, this);
                GridPane.setHgrow(card, Priority.ALWAYS);
                grid.add(card, index % 2, index / 2);
                index++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        scrollPane.setContent(grid);
        System.out.println("Load: category=" + currentCategory + " sort=" + currentSort);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void selectCategory(Label[] all, Label selected) {
        for (Label l : all) {
            if (l != null) l.getStyleClass().remove("catSelected");
        }
        if (!selected.getStyleClass().contains("catSelected"))
            selected.getStyleClass().add("catSelected");
    }
}