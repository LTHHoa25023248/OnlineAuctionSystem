package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.model.Auction;
import com.example.auctionmanagementsystem.model.AutoBid;
import com.example.auctionmanagementsystem.model.BidTransaction;
import com.example.auctionmanagementsystem.model.User;

import java.util.List;

public class AutoBidService {
    public void processAutoBids(Auction auction){
        //lay danh sach autobid trong phien dau gia
        List<AutoBid> autoBids=auction.getHisAutoBid();
        //Sap xep thoi gian dang ky-> ai dang ky truoc thi duoc uu tien dat truoc
        autoBids.sort((a,b)->a.getCreatedAt().compareTo((b.getCreatedAt())));
        boolean updated = true;
        // thuc hien lap den khi dat gia toi da, autobid ko tang gia duoc nua
        while(updated){
            updated=false;
            for(AutoBid auto : autoBids){
                User user=auto.getUser();
                //Neu nguoi nay dang la nguoi dan dau thi bo qua
                if(user.equals(auction.getHighestBidder()))
                  continue;
                //Tinh gia tiep theo= gia hien tai+buoc nhay(increment)
                double nextBid=auction.getCurrentPrice()+auto.getIncrement();
                //Chua vuot maxBid thi duoc phep autobid tiep
                if(nextBid<=auto.getMaxBid()){
                    //cap nhat gia hien tai
                    auction.setCurrentPrice(nextBid);
                    //cap nhat nguoi dan dau
                    auction.setHighestBidder(user);
                    //ls dau gia
                    auction.addBid(new BidTransaction(user,nextBid));
                    updated=true;

                }

                }
            }

        }

    }



