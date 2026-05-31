module com.example.auctionmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires MaterialFX;
    requires jakarta.mail;
    requires jdk.httpserver;
    requires java.net.http;


    opens com.example.auctionmanagementsystem to javafx.fxml;
    exports com.example.auctionmanagementsystem;
}