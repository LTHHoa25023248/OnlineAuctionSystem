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


//chạy thử bằng cách tự tạo sp
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

        String[] categories = {"Jewelry", "Watches", "Bags", "Fine Art", "Cars", "Others"};
        String[] names      = {"Rolex Daytona", "Chanel Classic", "Porsche 911",
                "Mona Lisa Print", "Patek Philippe", "Vintage Guitar"};

        int idx = (auctionId - 1) % categories.length;

        title.setText(names[idx]);
        price.setText(String.format("%,d USD", auctionId * 1000));
        totalBids.setText(auctionId + " bids");
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