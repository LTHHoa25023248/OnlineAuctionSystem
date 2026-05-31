package com.example.auctionmanagementsystem.controller;

import java.net.URL;

/**
 * ThemeManager — Quản lý chế độ Dark/Light theme toàn app.
 *
 * Singleton — dùng ThemeManager.getInstance() ở mọi nơi.
 *
 * CÁCH DÙNG: ThemeManager.getInstance().toggleTheme(scene);
 * ThemeManager.getInstance().applyTheme(scene, CssType.MAIN);
 * ThemeManager.getInstance().isDarkMode();
 *
 * CssType xác định bộ CSS dark nào dùng cho màn hình đó: MAIN → styles.css LOGIN →
 * loginsignupstyles.css ADMIN → adminstyles.css DETAIL → listingpagestyles.css PROFILE →
 * profilepagestyles.css
 *
 * Light mode luôn dùng lightstyles.css cho mọi màn hình.
 */
public class ThemeManager {

  // Singleton 
  private static ThemeManager instance;

  private ThemeManager() {}

  public static ThemeManager getInstance() {
    if (instance == null)
      instance = new ThemeManager();
    return instance;
  }

  // State 
  /** true = dark mode (mặc định khi khởi động) */
  private boolean darkMode = true;

  /**
   * Enum xác định loại CSS dark tương ứng với từng màn hình. Light mode luôn dùng lightstyles.css
   * bất kể CssType.
   */
  public enum CssType {
    MAIN, // styles.css — auction_list, deposit
    LOGIN, // loginsignupstyles.css — login, signup, forgotpass
    ADMIN, // adminstyles.css — AdminDashboard
    DETAIL, // listingpagestyles.css — auction_detail
    PROFILE // profilepagestyles.css — auction_profile
  }

  // CSS resolver 

  /**
   * Tìm file CSS trong classpath bằng 3 chiến lược. Giống resolveUrl() trong NavigationUtil.
   */
  private String resolveCss(String cssFileName) {
    URL url;

    // Chiến lược 1: relative từ package controller → lên 1 cấp → css/
    url = ThemeManager.class.getResource("../css/" + cssFileName);
    if (url != null)
      return url.toExternalForm();

    // Chiến lược 2: absolute path từ root classpath
    url = ThemeManager.class.getResource("/com/example/auctionmanagementsystem/css/" + cssFileName);
    if (url != null)
      return url.toExternalForm();

    // Chiến lược 3: ClassLoader — fallback cuối cùng
    url = ThemeManager.class.getClassLoader()
        .getResource("com/example/auctionmanagementsystem/css/" + cssFileName);
    if (url != null)
      return url.toExternalForm();

    System.err.println("[ThemeManager] NOT FOUND: " + cssFileName);
    return null;
  }

  /**
     * Lấy tên file CSS dark tương ứng với CssType.
     */
    private String getDarkCssFile(CssType type) {
        return switch (type) {
            case LOGIN   -> "loginsignupstyles.css";
            case ADMIN   -> "adminstyles.css";
            case DETAIL  -> "listingpagestyles.css";
            case PROFILE -> "profilepagestyles.css";
            default      -> "styles.css";
        };
    }

  // API công khai 

  /**
   * Toggle Dark/Light mode, áp dụng lên Scene với CssType.MAIN. Gọi từ themeButton trong
   * AuctionListController.
   */
  public void toggleTheme(javafx.scene.Scene scene) {
    darkMode = !darkMode;
    System.out.println("[ThemeManager] → " + (darkMode ? "DARK" : "LIGHT"));
    applyTheme(scene, CssType.MAIN);
  }

  /**
   * Áp dụng theme lên Scene với CssType chỉ định.
   *
   * Dark mode → CSS riêng theo từng màn hình Light mode → lightstyles.css cho mọi màn hình
   *
   * Gọi trong NavigationUtil.goTo() và openPopup() để theme nhất quán trên mọi màn hình.
   *
   * @param scene Scene cần áp dụng, null sẽ bỏ qua
   * @param type Loại CSS dark tương ứng với màn hình
   */
  public void applyTheme(javafx.scene.Scene scene, CssType type) {
    if (scene == null)
      return;

    String cssFile = darkMode ? getDarkCssFile(type) : "lightstyles.css";
    String cssUrl = resolveCss(cssFile);

    if (cssUrl == null) {
      System.err.println("[ThemeManager] Cannot apply: " + cssFile);
      return;
    }

    scene.getStylesheets().clear();
    scene.getStylesheets().add(cssUrl);
    System.out.println("[ThemeManager] Applied: " + cssFile);
  }

  /**
   * Overload không cần CssType — mặc định dùng MAIN. Dùng khi không biết loại màn hình
   */
  public void applyTheme(javafx.scene.Scene scene) {
    applyTheme(scene, CssType.MAIN);
  }

  /** @return true nếu đang dùng dark mode */
  public boolean isDarkMode() {
    return darkMode;
  }
}
