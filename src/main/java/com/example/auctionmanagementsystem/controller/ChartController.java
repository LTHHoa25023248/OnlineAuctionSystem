package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * ChartController — Popup biểu đồ Analytics (chart.fxml).
 *
 * Mở từ AdminDashboardController (handleAnalytics).
 *
 * Hiện tại hiển thị LineChart doanh thu theo tháng.
 * TODO: Kết nối TransactionDAO để lấy dữ liệu thật theo năm/tháng.
 */
public class ChartController {

    // ── FXML fields ───────────────────────────────────────────────────────────
    @FXML private LineChart<String, Number> revenueChart;    // biểu đồ đường
    @FXML private Label     totalRevenueLabel; // tổng doanh thu (TODO)
    @FXML private Label     monthRevenueLabel; // doanh thu tháng này (TODO)
    @FXML private Label     totalBidsLabel;    // tổng số bid (TODO)
    @FXML private Label     activeUsersLabel;  // user active (TODO)
    @FXML private ImageView closeButton;       // đóng popup

    @FXML
    public void initialize() {
        if (closeButton != null)
            closeButton.setOnMouseClicked(this::handleClose);
        loadChart();
    }

    /**
     * Vẽ biểu đồ doanh thu 12 tháng.
     * TODO: Thay dữ liệu cứng bằng:
     *   List<> data = TransactionDAO.getMonthlyRevenue(2025);
     *   data.forEach(r -> series.getData().add(
     *       new XYChart.Data<>(r.getMonth(), r.getAmount())));
     */
    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue 2025");

        // Dữ liệu mẫu theo tháng
        series.getData().addAll(
                new XYChart.Data<>("Jan", 3200),
                new XYChart.Data<>("Feb", 4100),
                new XYChart.Data<>("Mar", 3800),
                new XYChart.Data<>("Apr", 5200),
                new XYChart.Data<>("May", 4800),
                new XYChart.Data<>("Jun", 6100),
                new XYChart.Data<>("Jul", 5900),
                new XYChart.Data<>("Aug", 7200),
                new XYChart.Data<>("Sep", 6800),
                new XYChart.Data<>("Oct", 7800),
                new XYChart.Data<>("Nov", 8450),
                new XYChart.Data<>("Dec", 9100)
        );

        revenueChart.getData().add(series);
        revenueChart.setCreateSymbols(true); // hiện điểm tại mỗi data point
        revenueChart.setAnimated(true);      // animation khi load
    }

    /** Đóng popup chart */
    @FXML
    private void handleClose(MouseEvent e) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}