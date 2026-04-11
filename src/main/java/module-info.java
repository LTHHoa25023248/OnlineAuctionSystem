module com.example.auctionmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.auctionmanagementsystem to javafx.fxml;
    exports com.example.auctionmanagementsystem;
}