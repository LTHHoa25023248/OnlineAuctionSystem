package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public class NavigationUtil {

    // ── FXML constants ────────────────────────────────────────────────────────
    public static final String LOGIN          = "View/auction_login.fxml";
    public static final String SIGNUP         = "View/auction_signup.fxml";
    public static final String FORGOT_PASS    = "View/forgotpass.fxml";
    public static final String AUCTION_LIST   = "View/auction_list.fxml";
    public static final String AUCTION_DETAIL = "View/auction_detail.fxml";
    public static final String PROFILE        = "View/auction_profile.fxml";
    public static final String ADMIN          = "View/AdminDashboard.fxml";
    public static final String MAIN_LAYOUT    = "View/main_layout.fxml";
    public static final String DEPOSIT        = "View/deposit.fxml";

    public static final String AUCTION_CARD   = "View/components/auction_card.fxml";
    public static final String ADD_LISTING    = "View/components/add_listing.fxml";
    public static final String SORTING_MENU   = "View/components/sortingmenu.fxml";
    public static final String CHART          = "View/components/chart.fxml";
    public static final String COMMENT        = "View/components/comment.fxml";

    // ── Scene switchers ───────────────────────────────────────────────────────

    public static void goTo(Node source, String fxmlPath) {
        try {
            Stage stage = (Stage) source.getScene().getWindow();
            goTo(stage, fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void goTo(Stage stage, String fxmlPath) {
        try {
            URL url = resolveUrl(fxmlPath);
            if (url == null) {
                System.err.println("[NavigationUtil] FXML not found: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(url);
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Popup ─────────────────────────────────────────────────────────────────

    public static <T> T openPopup(Node source, String fxmlPath, String title) {
        try {
            URL url = resolveUrl(fxmlPath);
            if (url == null) {
                System.err.println("[NavigationUtil] FXML not found: " + fxmlPath);
                return null;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(source.getScene().getWindow());
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setTitle(title);
            popup.setScene(new Scene(root));
            popup.showAndWait();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Resource resolver ─────────────────────────────────────────────────────

    private static URL resolveUrl(String fxmlPath) {
        URL url = NavigationUtil.class.getResource("../" + fxmlPath);
        if (url != null) return url;

        url = NavigationUtil.class.getResource(
                "/com/example/auctionmanagementsystem/" + fxmlPath);
        if (url != null) return url;

        url = NavigationUtil.class.getClassLoader().getResource(
                "com/example/auctionmanagementsystem/" + fxmlPath);
        if (url != null) return url;

        System.err.println("[NavigationUtil] All 3 strategies failed for: " + fxmlPath);
        return null;
    }
}