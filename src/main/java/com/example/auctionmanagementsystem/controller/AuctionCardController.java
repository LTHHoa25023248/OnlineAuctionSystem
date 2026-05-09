//package com.example.auctionmanagementsystem.controller;
//
//import javafx.fxml.FXML;
//import javafx.scene.input.MouseEvent;
//
///**
// * Controller cho View/components/auction_card.fxml
// * Được load động bởi AuctionListController.
// *
// * FXML: thêm onMouseClicked="#onCardClick" vào root container của auction_card.fxml
// */
//public class AuctionCardController {
//
//    private AuctionListController parentController;
//    private int auctionId;
//
//    /** Gọi sau khi load FXML để truyền dữ liệu vào card */
//    public void setAuction(int auctionId, AuctionListController parent) {
//        this.auctionId        = auctionId;
//        this.parentController = parent;
//
//        /* ── Điền dữ liệu vào các Label/ImageView trong card ────────────────
//         *   titleLabel.setText(auction.getTitle());
//         *   priceLabel.setText(auction.getCurrentBid() + " $");
//         *   categoryLabel.setText(auction.getCategory());
//         *   if (auction.getImagePath() != null)
//         *       thumbnail.setImage(new Image(auction.getImagePath()));
//         * ─────────────────────────────────────────────────────────────────── */
//    }
//
//    @FXML
//    private void onCardClick(MouseEvent event) {
//        if (parentController != null)
//            parentController.openAuctionDetail(auctionId);
//    }
//}




//package com.example.auctionmanagementsystem.controller;
//
//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.MouseEvent;
//
//public class AuctionCardController {
//
//    @FXML private ImageView image;
//    @FXML private Label     title;
//    @FXML private Label     price;
//    @FXML private Label     totalBids;
//    @FXML private Label     timeLeft;
//    @FXML private Label     categoryLabel;
//    @FXML private Label     popularNowLabel;
//
//    private AuctionListController parentController;
//    private int auctionId;
//
//    public void setAuction(int auctionId, AuctionListController parent) {
//        this.auctionId        = auctionId;
//        this.parentController = parent;
//
//        // Dữ liệu mẫu — thay bằng Auction object từ DAO sau
//        title.setText("Sản phẩm " + auctionId);
//        price.setText(auctionId * 1000 + " USD");
//        totalBids.setText("Total Bids :   " + auctionId);
//        timeLeft.setText("Ending In :  " + auctionId + " D 07 Hrs");
//        categoryLabel.setText("Jewelry");
//        popularNowLabel.setText("Popular Now");
//
//        // Ảnh mẫu — thay bằng đường dẫn thật từ DB sau
//        // image.setImage(new Image(auction.getImagePath()));
//    }
//
//    @FXML
//    private void onCardClick(MouseEvent event) {
//        if (parentController != null)
//            parentController.openAuctionDetail(auctionId);
//    }
//}

package com.example.auctionmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class AuctionCardController {

    @FXML private ImageView image;
    @FXML private Label     title;
    @FXML private Label     price;
    @FXML private Label     totalBids;
    @FXML private Label     timeLeft;
    @FXML private Label     categoryLabel;
    @FXML private Label     popularNowLabel;

    private AuctionListController parentController;
    private int auctionId;

    public void setAuction(int auctionId, AuctionListController parent) {
        this.auctionId        = auctionId;
        this.parentController = parent;

        // Dữ liệu mẫu với các category khác nhau
        String[] categories = {"Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others"};
        String[] names = {"Rolex Daytona", "Chanel Classic", "Porsche 911", "Mona Lisa Print", "Patek Philippe", "Vintage Guitar"};

        int idx = (auctionId - 1) % categories.length;

        title.setText(names[idx]);
        price.setText(auctionId * 1000 + " USD");
        totalBids.setText("Total Bids :   " + auctionId);
        timeLeft.setText("Ending In :  " + auctionId + " D 07 Hrs");
        categoryLabel.setText(categories[idx]);
        popularNowLabel.setText("Popular Now");
    }

    @FXML
    private void onCardClick(MouseEvent event) {
        if (parentController != null)
            parentController.openAuctionDetail(auctionId);
    }
}