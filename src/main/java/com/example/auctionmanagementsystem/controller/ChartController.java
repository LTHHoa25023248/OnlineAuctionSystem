package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ChartController {

    @FXML private LineChart<String, Number> revenueChart;
    @FXML private Label    totalRevenueLabel;
    @FXML private Label    monthRevenueLabel;
    @FXML private Label    totalBidsLabel;
    @FXML private Label    activeUsersLabel;
    @FXML private ImageView closeButton;

    @FXML
    public void initialize() {
        closeButton.setOnMouseClicked(this::handleClose);
        loadChart();
    }

    private void loadChart() {
        // Thay bằng DAO thực sau
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue 2025");

        series.getData().add(new XYChart.Data<>("Jan", 3200));
        series.getData().add(new XYChart.Data<>("Feb", 4100));
        series.getData().add(new XYChart.Data<>("Mar", 3800));
        series.getData().add(new XYChart.Data<>("Apr", 5200));
        series.getData().add(new XYChart.Data<>("May", 4800));
        series.getData().add(new XYChart.Data<>("Jun", 6100));
        series.getData().add(new XYChart.Data<>("Jul", 5900));
        series.getData().add(new XYChart.Data<>("Aug", 7200));
        series.getData().add(new XYChart.Data<>("Sep", 6800));
        series.getData().add(new XYChart.Data<>("Oct", 7800));
        series.getData().add(new XYChart.Data<>("Nov", 8450));
        series.getData().add(new XYChart.Data<>("Dec", 9100));

        revenueChart.getData().add(series);
        revenueChart.setCreateSymbols(true);
        revenueChart.setAnimated(true);
    }

    @FXML
    private void handleClose(MouseEvent e) {
        ((Stage) closeButton.getScene().getWindow()).close();
    }
}