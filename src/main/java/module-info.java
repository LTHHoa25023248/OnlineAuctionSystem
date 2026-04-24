module com.example.auctionmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.auctionmanagementsystem to javafx.fxml;
    exports com.example.auctionmanagementsystem;
    opens com.example.auctionmanagementsystem.controller to javafx.fxml;
    exports com.example.auctionmanagementsystem.controller;
}