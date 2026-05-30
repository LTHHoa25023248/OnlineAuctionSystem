package com.example.auctionmanagementsystem.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * Dialog thông báo kết quả đấu giá – tự style theo theme tối/vàng của hệ thống.
 *
 * Thay thế cho Alert mặc định (hộp trắng kiểu Windows). Không phụ thuộc file CSS,
 * tất cả màu sắc được set inline để drop-in chạy ngay ở bất kỳ controller nào.
 *
 * Cách dùng:
 *   ResultDialog.showWin(owner,    itemName, finalPrice, remainingBalance);
 *   ResultDialog.showLose(owner,   itemName, finalPrice, winnerLabelOrNull);
 *   ResultDialog.showSeller(owner, itemName, finalPrice, newBalance);
 *
 * owner: lấy từ controller bằng  someNode.getScene().getWindow()  (có thể null).
 */
public final class ResultDialog {

    private ResultDialog() {}

    // ── Palette lấy từ styles.css ─────────────────────────────────────────────
    private static final String BG_CARD     = "#131720";
    private static final String BG_INNER    = "#1A1F2E";
    private static final String BG_DEEP     = "#0D1018";
    private static final String GOLD_BRIGHT = "#F0C060";
    private static final String GOLD_MID    = "#D4A83A";
    private static final String TEXT_PRIM   = "#F2F0EC";
    private static final String TEXT_SEC    = "#A8A49C";
    private static final String TEXT_MUTED  = "#5C5850";
    private static final String GREEN       = "#3DBA7F";
    private static final String RED         = "#E05454";
    private static final String AMBER       = "#E8A020";

    private static final String FONT_SERIF = "'Georgia', 'Palatino Linotype', serif";
    private static final String FONT_BODY  = "'Segoe UI', 'SF Pro Display', sans-serif";

    private enum Type { WIN, LOSE, SELLER }

    // ── API công khai ─────────────────────────────────────────────────────────

    /** Bidder thắng phiên. balance = số dư còn lại sau thanh toán (truyền giá trị < 0 để ẩn). */
    public static void showWin(Window owner, String itemName, double finalPrice, double balance) {
        show(owner, Type.WIN,
                "★ Congratulations! ★",
                "You won this auction",
                itemName, finalPrice,
                balance >= 0 ? "Remaining balance" : null, balance,
                "Payment has been processed automatically.",
                GREEN, "🏆");
    }

    /** Bidder thua phiên. winnerInfo có thể là "Bidder #5" hoặc null nếu không muốn hiện. */
    public static void showLose(Window owner, String itemName, double finalPrice, String winnerInfo) {
        String sub = (winnerInfo == null || winnerInfo.isBlank())
                ? "This auction was won by someone else"
                : "Winner: " + winnerInfo;
        show(owner, Type.LOSE,
                "Better luck next time!",
                sub,
                itemName, finalPrice,
                null, -1,
                "Don't give up — there are many more auctions waiting for you.",
                RED, "🔔");
    }

    /** Seller bán được hàng. balance = số dư hiện tại sau khi nhận tiền. */
    public static void showSeller(Window owner, String itemName, double finalPrice, double balance) {
        show(owner, Type.SELLER,
                "Item Sold!",
                "Your item has a new owner",
                itemName, finalPrice,
                balance >= 0 ? "Current balance" : null, balance,
                "The sale amount has been added to your account.",
                AMBER, "💰");
    }

    /** Một dòng phiên thắng dùng cho dialog tổng hợp. */
    public static final class WinItem {
        public final String title;
        public final double amount;
        public WinItem(String title, double amount) { this.title = title; this.amount = amount; }
    }

    /**
     * Dialog tổng hợp khi thắng NHIỀU phiên cùng lúc (thay cho popup "You won N auction(s)!").
     * Nếu danh sách chỉ có 1 phần tử, nên gọi showWin(...) thay vì hàm này.
     */
    public static void showWinSummary(Window owner, java.util.List<WinItem> wins) {
        if (wins == null || wins.isEmpty()) return;

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) { stage.initOwner(owner); stage.initModality(Modality.APPLICATION_MODAL); }
        stage.setTitle("You won " + wins.size() + " auction(s)!");

        Label icon = new Label("🏆");
        icon.setStyle("-fx-font-size: 30px;");
        StackPane badge = new StackPane(icon);
        badge.setMinSize(64, 64); badge.setMaxSize(64, 64);
        badge.setStyle("-fx-background-color: linear-gradient(to bottom right, " + GREEN + "33, " + GREEN + "11);" +
                "-fx-background-radius: 999; -fx-border-color: " + GREEN + "; -fx-border-width: 1.5; -fx-border-radius: 999;");

        Label titleLbl = new Label("★ Congratulations! ★");
        titleLbl.setStyle("-fx-font-family: " + FONT_SERIF + "; -fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: " + GOLD_BRIGHT + ";");
        Label subLbl = new Label("You won " + wins.size() + " auction(s)");
        subLbl.setStyle("-fx-font-family: " + FONT_BODY + "; -fx-font-size: 13px; -fx-text-fill: " + TEXT_SEC + ";");
        VBox head = new VBox(4, titleLbl, subLbl);
        head.setAlignment(Pos.CENTER);

        VBox listBox = new VBox(8);
        for (WinItem w : wins) {
            Label name = new Label(w.title);
            name.setWrapText(true);
            name.setStyle("-fx-font-family: " + FONT_BODY + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIM + ";");
            Label amt = new Label(String.format("%,.2f USD", w.amount));
            amt.setStyle("-fx-font-family: " + FONT_BODY + "; -fx-font-size: 12.5px; -fx-text-fill: " + GREEN + ";");
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            HBox r = new HBox(10, name, spacer, amt);
            r.setAlignment(Pos.CENTER_LEFT);
            r.setPadding(new Insets(10, 14, 10, 14));
            r.setStyle("-fx-background-color: " + BG_INNER + "; -fx-background-radius: 8;" +
                    "-fx-border-color: " + GOLD_MID + "22; -fx-border-width: 1; -fx-border-radius: 8;");
            listBox.getChildren().add(r);
        }
        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(listBox);
        sp.setFitToWidth(true);
        sp.setPrefHeight(Math.min(60 + wins.size() * 56, 280));
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        Button ok = new Button("OK");
        ok.setDefaultButton(true);
        String okBase = "-fx-font-family: " + FONT_BODY + "; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-padding: 9 34 9 34; -fx-background-radius: 8; -fx-cursor: hand;";
        String okIdle = "-fx-background-color: linear-gradient(to bottom, " + GOLD_BRIGHT + ", " + GOLD_MID + "); -fx-text-fill: " + BG_DEEP + ";";
        String okHover = "-fx-background-color: linear-gradient(to bottom, #F8D070, #ECC050); -fx-text-fill: " + BG_DEEP + ";";
        ok.setStyle(okBase + okIdle);
        ok.setOnMouseEntered(e -> ok.setStyle(okBase + okHover));
        ok.setOnMouseExited(e -> ok.setStyle(okBase + okIdle));
        ok.setOnAction(e -> closeWithFade(stage));
        HBox actions = new HBox(ok);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region topBar = new Region();
        topBar.setMinHeight(4); topBar.setMaxHeight(4);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, " + GREEN + ", " + GOLD_BRIGHT + "); -fx-background-radius: 14 14 0 0;");

        VBox body = new VBox(16, badge, head, sp, actions);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(22, 26, 22, 26));
        VBox card = new VBox(topBar, body);
        card.setMaxWidth(440);
        card.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 14;" +
                "-fx-border-color: " + GOLD_MID + "55; -fx-border-width: 1; -fx-border-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 34, 0, 0, 10);");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(26));
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) closeWithFade(stage); });
        final double[] off = new double[2];
        card.setOnMousePressed(e -> { off[0] = e.getScreenX() - stage.getX(); off[1] = e.getScreenY() - stage.getY(); });
        card.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - off[0]); stage.setY(e.getScreenY() - off[1]); });
        stage.setScene(scene);

        card.setOpacity(0); card.setScaleX(0.92); card.setScaleY(0.92);
        stage.setOnShown(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(220), card);
            fade.setFromValue(0); fade.setToValue(1);
            ScaleTransition scale = new ScaleTransition(Duration.millis(260), card);
            scale.setFromX(0.92); scale.setFromY(0.92); scale.setToX(1); scale.setToY(1);
            scale.setInterpolator(Interpolator.SPLINE(0.2, 0.9, 0.3, 1.0));
            new ParallelTransition(fade, scale).play();
        });
        stage.showAndWait();
    }

    // ── Phần dựng giao diện ───────────────────────────────────────────────────

    private static void show(Window owner, Type type, String title, String subtitle,
                             String itemName, double finalPrice,
                             String balanceLabel, double balance,
                             String footer, String accent, String glyph) {

        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        stage.setTitle(title);

        // ── Huy hiệu icon tròn ở đầu ─────────────────────────────────────────
        Label icon = new Label(glyph);
        icon.setStyle("-fx-font-size: 30px;");
        StackPane badge = new StackPane(icon);
        badge.setMinSize(64, 64);
        badge.setMaxSize(64, 64);
        badge.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + accent + "33, " + accent + "11);" +
                        "-fx-background-radius: 999;" +
                        "-fx-border-color: " + accent + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 999;");

        // ── Tiêu đề + phụ đề ──────────────────────────────────────────────────
        Label titleLbl = new Label(title);
        titleLbl.setStyle(
                "-fx-font-family: " + FONT_SERIF + ";" +
                        "-fx-font-size: 23px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + (type == Type.LOSE ? TEXT_PRIM : GOLD_BRIGHT) + ";");

        Label subLbl = new Label(subtitle);
        subLbl.setWrapText(true);
        subLbl.setStyle(
                "-fx-font-family: " + FONT_BODY + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-text-fill: " + TEXT_SEC + ";");

        VBox head = new VBox(4, titleLbl, subLbl);
        head.setAlignment(Pos.CENTER);

        // ── Khối chi tiết (tên SP, giá, số dư) ────────────────────────────────
        VBox detail = new VBox(10);
        detail.setPadding(new Insets(16, 18, 16, 18));
        detail.setStyle(
                "-fx-background-color: " + BG_DEEP + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + accent + "44;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;");

        detail.getChildren().add(row("Item", itemName == null ? "—" : itemName, TEXT_PRIM, false));
        detail.getChildren().add(row("Final Price", String.format("%,.2f USD", finalPrice), accent, true));
        if (balanceLabel != null) {
            detail.getChildren().add(divider());
            detail.getChildren().add(row(balanceLabel, String.format("%,.2f USD", balance), GREEN, false));
        }

        // ── Footer text ───────────────────────────────────────────────────────
        Label footerLbl = new Label(footer);
        footerLbl.setWrapText(true);
        footerLbl.setStyle(
                "-fx-font-family: " + FONT_BODY + ";" +
                        "-fx-font-size: 11.5px;" +
                        "-fx-text-fill: " + TEXT_MUTED + ";");

        // ── Nút OK ──────────────────────────────────────────────────────────
        Button ok = new Button("OK");
        ok.setDefaultButton(true);
        String okIdle, okHover;
        if (type == Type.LOSE) {
            okIdle  = "-fx-background-color: transparent; -fx-text-fill: " + GOLD_BRIGHT + ";" +
                    "-fx-border-color: " + GOLD_MID + "; -fx-border-width: 1; -fx-border-radius: 8;";
            okHover = "-fx-background-color: " + GOLD_MID + "22; -fx-text-fill: " + GOLD_BRIGHT + ";" +
                    "-fx-border-color: " + GOLD_BRIGHT + "; -fx-border-width: 1; -fx-border-radius: 8;";
        } else {
            okIdle  = "-fx-background-color: linear-gradient(to bottom, " + GOLD_BRIGHT + ", " + GOLD_MID + ");" +
                    "-fx-text-fill: " + BG_DEEP + ";";
            okHover = "-fx-background-color: linear-gradient(to bottom, #F8D070, #ECC050);" +
                    "-fx-text-fill: " + BG_DEEP + ";";
        }
        String okBase = "-fx-font-family: " + FONT_BODY + "; -fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-padding: 9 34 9 34; -fx-background-radius: 8; -fx-cursor: hand;";
        ok.setStyle(okBase + okIdle);
        ok.setOnMouseEntered(e -> ok.setStyle(okBase + okHover));
        ok.setOnMouseExited(e -> ok.setStyle(okBase + okIdle));
        ok.setOnAction(e -> closeWithFade(stage));

        HBox actions = new HBox(ok);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // ── Vạch màu accent trên đỉnh card ────────────────────────────────────
        Region topBar = new Region();
        topBar.setMinHeight(4);
        topBar.setMaxHeight(4);
        topBar.setStyle(
                "-fx-background-color: linear-gradient(to right, " + accent + ", " + GOLD_BRIGHT + ");" +
                        "-fx-background-radius: 14 14 0 0;");

        // ── Thân card ─────────────────────────────────────────────────────────
        VBox body = new VBox(16, badge, head, detail, footerLbl, actions);
        body.setAlignment(Pos.CENTER);
        body.setPadding(new Insets(22, 26, 22, 26));

        VBox card = new VBox(topBar, body);
        card.setMaxWidth(420);
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + GOLD_MID + "55;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 34, 0, 0, 10);");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(26)); // chừa chỗ cho đổ bóng
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) closeWithFade(stage); });

        // Cho phép kéo di chuyển (vì cửa sổ không có thanh tiêu đề)
        final double[] off = new double[2];
        card.setOnMousePressed(e -> { off[0] = e.getScreenX() - stage.getX(); off[1] = e.getScreenY() - stage.getY(); });
        card.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - off[0]); stage.setY(e.getScreenY() - off[1]); });

        stage.setScene(scene);

        // ── Animation xuất hiện (fade + scale) ───────────────────────────────
        card.setOpacity(0);
        card.setScaleX(0.92);
        card.setScaleY(0.92);
        stage.setOnShown(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(220), card);
            fade.setFromValue(0); fade.setToValue(1);
            ScaleTransition scale = new ScaleTransition(Duration.millis(260), card);
            scale.setFromX(0.92); scale.setFromY(0.92);
            scale.setToX(1); scale.setToY(1);
            scale.setInterpolator(Interpolator.SPLINE(0.2, 0.9, 0.3, 1.0));
            new ParallelTransition(fade, scale).play();
        });

        stage.showAndWait();
    }

    private static HBox row(String key, String value, String valueColor, boolean big) {
        Label k = new Label(key);
        k.setStyle("-fx-font-family: " + FONT_BODY + "; -fx-font-size: 12.5px; -fx-text-fill: " + TEXT_SEC + ";");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setStyle("-fx-font-family: " + FONT_BODY + ";" +
                "-fx-font-size: " + (big ? "17px" : "13px") + ";" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + valueColor + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        HBox h = new HBox(10, k, spacer, v);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private static Region divider() {
        Region d = new Region();
        d.setMinHeight(1);
        d.setMaxHeight(1);
        d.setStyle("-fx-background-color: " + GOLD_MID + "22;");
        return d;
    }

    private static void closeWithFade(Stage stage) {
        Region card = (Region) ((StackPane) stage.getScene().getRoot()).getChildren().get(0);
        FadeTransition fade = new FadeTransition(Duration.millis(150), card);
        fade.setFromValue(1); fade.setToValue(0);
        fade.setOnFinished(e -> stage.close());
        fade.play();
    }
}