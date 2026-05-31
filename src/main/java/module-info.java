module com.example.auctionmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires MaterialFX;
    requires jakarta.mail;
    requires org.apache.commons.lang3;
    requires jdk.httpserver;
    requires com.google.gson;
    requires java.net.http;

    opens com.example.auctionmanagementsystem            to javafx.fxml;
    opens com.example.auctionmanagementsystem.controller to javafx.fxml;
    opens com.example.auctionmanagementsystem.model      to javafx.base, javafx.fxml, com.google.gson;

    exports com.example.auctionmanagementsystem;
    exports com.example.auctionmanagementsystem.controller;

}
