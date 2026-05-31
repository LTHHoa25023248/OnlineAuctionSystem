package com.example.auctionmanagementsystem.server;

import com.example.auctionmanagementsystem.server.handler.AdminHandler;
import com.example.auctionmanagementsystem.server.handler.AuctionHandler;
import com.example.auctionmanagementsystem.server.handler.AuthHandler;
import com.example.auctionmanagementsystem.server.handler.ItemHandler;
import com.example.auctionmanagementsystem.server.handler.UserHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class AuctionServer {
    private static final int PORT = 8080;
    private static HttpServer server;
    private AuctionServer() {}
    public static void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        AuthHandler auth = new AuthHandler();
        server.createContext("/auth/login",          auth);
        server.createContext("/auth/register",       auth);
        server.createContext("/auth/check-username", auth);
        server.createContext("/auth/check-email",    auth);


        AuctionHandler auction = new AuctionHandler();
        server.createContext("/auction/list",    auction);
        server.createContext("/auction/detail",  auction);
        server.createContext("/auction/bid",     auction);
        server.createContext("/auction/create",  auction);
        server.createContext("/auction/approve", auction);
        server.createContext("/auction/reject",  auction);
        server.createContext("/auction/end",     auction);
        server.createContext("/auction/autobid", auction);

        UserHandler user = new UserHandler();
        server.createContext("/user/balance", user);
        server.createContext("/user/profile", user);
        server.createContext("/user/wins",    user);

        AdminHandler admin = new AdminHandler();
        server.createContext("/admin/stats",    admin);
        server.createContext("/admin/auctions", admin);
        server.createContext("/admin/users",    admin);
        server.createContext("/admin/user/ban", admin);

        ItemHandler item = new ItemHandler();
        server.createContext("/item/update", item);
        server.createContext("/item/delete", item);
        //thread pool de xu ly duoc nhieu reqqust cung 1 luc, newCached tu tao thread khi can
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("[AuctionServer] Started on port " + PORT);
    }
    //dong server khi web dong lai
    public static void stop() {
        if (server != null) {
            server.stop(1);
            System.out.println("[AuctionServer] Stopped.");
        }
    }
}
