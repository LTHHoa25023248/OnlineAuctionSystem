package com.example.auctionmanagementsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private TableView listingsTable;
    @FXML private Label totalListingsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label revenueLabel;
    @FXML private Label usernameLabel;

    @FXML private TableColumn colItem;
    @FXML private TableColumn colCategory;
    @FXML private TableColumn colSeller;
    @FXML private TableColumn colBid;
    @FXML private TableColumn colStatus;
    @FXML private TableColumn colActions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        totalListingsLabel.setText("1,248");
        activeAuctionsLabel.setText("342");
        totalUsersLabel.setText("5,831");
        revenueLabel.setText("$84,210");
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        // TODO: navigate to login screen
    }

    @FXML
    public void handleHome(javafx.scene.input.MouseEvent e) {}

    @FXML
    public void handleListings(javafx.scene.input.MouseEvent e) {}

    @FXML
    public void handleUsers(javafx.scene.input.MouseEvent e) {}

    @FXML
    public void handleAnalytics(javafx.scene.input.MouseEvent e) {}

    @FXML
    public void handleReports(javafx.scene.input.MouseEvent e) {}

    @FXML
    public void handleSettings(javafx.scene.input.MouseEvent e) {}

    @FXML
    public void filterAll(ActionEvent e) {}

    @FXML
    public void filterJewelry(ActionEvent e) {}

    @FXML
    public void filterWatches(ActionEvent e) {}

    @FXML
    public void filterCars(ActionEvent e) {}

    @FXML
    public void filterOthers(ActionEvent e) {}
}