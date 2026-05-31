package com.example.auctionmanagementsystem.service;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.model.Auction;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


//chay ngam cu moi 30sde kiem tra va dong cac phien dau gia da het gio
public class AuctionScheduler {
     //singleton chi tao 1 instance duy nhat trong toan app
    private static AuctionScheduler instance;
    private final AuctionDAO auctionDao;
    private final AuctionService auctionService;
    private final PaymentService paymentService;

    private AuctionScheduler() {
        this.auctionDao = new AuctionDAO();
        this.auctionService = new AuctionService();
        this.paymentService = new PaymentService();
    }

    AuctionScheduler(AuctionDAO auctionDao, AuctionService auctionService, PaymentService paymentService) {
        this.auctionDao = auctionDao;
        this.auctionService = auctionService;
        this.paymentService = paymentService;
    }

    // ScheduledExecutorService-> thread chay ngam thuc hien theo lich trinh
    private ScheduledExecutorService scheduler;
   // Constructor private ngan tao moi ben ngoai
    public static synchronized AuctionScheduler getInstance() {
        if (instance == null) {
            instance = new AuctionScheduler();
        }
        return instance;
    }

    public void start() {
        //tao 1 thread chay ngam de kiem tra va dong cac phien dau gia het gio
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "auction-scheduler");
            // tat khi web tat
            thread.setDaemon(true);
            return thread;
        });
         //chay ngay lap tuc, delay =0, khoang cach giua cac lan chay la 30s)
        scheduler.scheduleAtFixedRate( () -> closeExpiredAuctions(),0,30,TimeUnit.SECONDS);

        System.out.println("[AuctionScheduler] Started — checking expired auctions every 30 seconds");
    }
    //dung lai khi thoat web
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("[AuctionScheduler] Stopped");
        }
    }
    //tim va dong tat ca cac auction da qua thoi gian ket thuc endTime, goi tu dong 30s 1 lan
    void closeExpiredAuctions() {
        Connection connect = null;
        try {
            connect = DatabaseConnection.getConnection();
        // lay ds tat ca cac auction dang mo 
     List<Auction> runningAuctions = auctionDao.selectOpenAuctions(connect);
           LocalDateTime now = LocalDateTime.now();
            for (Auction auction : runningAuctions) {
                // neu endTime da qua gio thi dong phien dau gia
                if (auction.getEndTime() != null && now.isAfter(auction.getEndTime())) {
                    try {
                        //dong phien dau gia, cap nhap trang thai
                        auctionService.endAuction(connect, auction);
                        System.out.println("[AuctionScheduler] Closed auction ID=" + auction.getId());
                    } catch (Exception e) {
                        // 1 acution bi loi thi van tiep tuc dong cac auction con lai duoc
                        System.err.println("[AuctionScheduler] Failed to close auction ID="
                                + auction.getId() + ": " + e.getMessage());
                    }
                }
            }
    } catch (Exception e) {
            System.err.println("[AuctionScheduler] Error during check: " + e.getMessage());
        } finally {

            if (connect != null) {
                try {
                    connect.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
