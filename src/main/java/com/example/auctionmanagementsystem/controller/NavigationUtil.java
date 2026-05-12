package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

/**
 * NavigationUtil — Tiện ích điều hướng màn hình toàn app.
 *
 * Cung cấp 2 kiểu chuyển màn hình:
 *   1. goTo()      → thay thế toàn bộ scene hiện tại (chuyển trang)
 *   2. openPopup() → mở cửa sổ popup modal
 *
 * Mỗi màn hình tự động nhận đúng CssType để ThemeManager
 * áp dụng stylesheet phù hợp.
 */
public class NavigationUtil {

    // ── Đường dẫn FXML ───────────────────────────────────────────────────────
    public static final String LOGIN          = "View/auction_login.fxml";
    public static final String SIGNUP         = "View/auction_signup.fxml";
    public static final String FORGOT_PASS    = "View/forgotpass.fxml";
    public static final String AUCTION_LIST   = "View/auction_list.fxml";
    public static final String AUCTION_DETAIL = "View/auction_detail.fxml";
    public static final String PROFILE        = "View/auction_profile.fxml";
    public static final String ADMIN          = "View/AdminDashboard.fxml";
    public static final String MAIN_LAYOUT    = "View/main_layout.fxml";
    public static final String DEPOSIT        = "View/deposit.fxml";

    public static final String AUCTION_CARD  = "View/components/auction_card.fxml";
    public static final String ADD_LISTING   = "View/components/add_listing.fxml";
    public static final String SORTING_MENU  = "View/components/sortingmenu.fxml";
    public static final String CHART         = "View/components/chart.fxml";
    public static final String COMMENT       = "View/components/comment.fxml";

    // ── Scene switcher ────────────────────────────────────────────────────────

    /**
     * Chuyển màn hình, tự xác định CssType từ fxmlPath.
     *
     * @param source   Node bất kỳ đang trên Stage cần chuyển
     * @param fxmlPath Đường dẫn FXML đích
     */
    public static void goTo(Node source, String fxmlPath) {
        try {
            Stage stage = (Stage) source.getScene().getWindow();
            goTo(stage, fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Chuyển màn hình trực tiếp trên Stage.
     * Tự xác định CssType từ fxmlPath để áp đúng stylesheet.
     * Tái sử dụng Scene cũ, không tạo Stage mới.
     *
     * @param stage    Stage cần cập nhật
     * @param fxmlPath Đường dẫn FXML đích
     */
    public static void goTo(Stage stage, String fxmlPath) {
        try {
            URL url = resolveUrl(fxmlPath);
            if (url == null) {
                System.err.println("[NavigationUtil] FXML not found: " + fxmlPath);
                return;
            }
            Parent root  = FXMLLoader.load(url);
            Scene  scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            // Áp dụng theme với CssType tương ứng màn hình
            ThemeManager.getInstance().applyTheme(scene, getCssType(fxmlPath));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Popup opener ──────────────────────────────────────────────────────────

    /**
     * Mở popup modal, áp dụng theme với CssType tương ứng.
     * Popup cũng nhận đúng theme như màn hình chính.
     *
     * Dùng StageStyle.UNDECORATED — FXML root phải có background color đặc
     * (bọc trong StackPane có -fx-background-color).
     *
     * @param source   Node để xác định cửa sổ cha
     * @param fxmlPath Đường dẫn FXML
     * @param title    Tiêu đề cửa sổ
     * @param <T>      Kiểu Controller trả về
     * @return Controller của popup, null nếu load thất bại
     */
    public static <T> T openPopup(Node source, String fxmlPath, String title) {
        try {
            URL url = resolveUrl(fxmlPath);
            if (url == null) {
                System.err.println("[NavigationUtil] FXML not found: " + fxmlPath);
                return null;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent     root   = loader.load();

            // Tạo Scene cho popup và áp dụng theme ngay
            Scene popupScene = new Scene(root);
            ThemeManager.getInstance().applyTheme(popupScene, getCssType(fxmlPath));

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(source.getScene().getWindow());
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setTitle(title);
            popup.setScene(popupScene);
            popup.showAndWait();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── CssType resolver ──────────────────────────────────────────────────────

    /**
     * Xác định CssType từ đường dẫn FXML.
     *
     * Mapping:
     *   login / signup / forgot → LOGIN   → loginsignupstyles.css
     *   Admin                   → ADMIN   → adminstyles.css
     *   detail                  → DETAIL  → listingpagestyles.css
     *   profile                 → PROFILE → profilepagestyles.css
     *   còn lại                 → MAIN    → styles.css
     */
    private static ThemeManager.CssType getCssType(String fxmlPath) {
        if (fxmlPath == null) return ThemeManager.CssType.MAIN;
        if (fxmlPath.contains("login")  ||
                fxmlPath.contains("signup") ||
                fxmlPath.contains("forgot"))  return ThemeManager.CssType.LOGIN;
        if (fxmlPath.contains("Admin"))   return ThemeManager.CssType.ADMIN;
        if (fxmlPath.contains("detail"))  return ThemeManager.CssType.DETAIL;
        if (fxmlPath.contains("profile")) return ThemeManager.CssType.PROFILE;
        return ThemeManager.CssType.MAIN;
    }

    // ── URL resolver ──────────────────────────────────────────────────────────

    /**
     * Thử 3 chiến lược để tìm file FXML trong classpath.
     *
     * Chiến lược 1: Relative path từ vị trí class NavigationUtil
     * Chiến lược 2: Absolute path từ gốc classpath
     * Chiến lược 3: ClassLoader (dùng khi module system can thiệp)
     */
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