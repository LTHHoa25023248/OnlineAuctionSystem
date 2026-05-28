package com.example.auctionmanagementsystem.observer;

import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionNotifier implements Subject {

    private final CopyOnWriteArrayList<Observer> observers;

    private AuctionNotifier() {
        this.observers = new CopyOnWriteArrayList<>();
    }

    private static class InstanceHolder {
        private static final AuctionNotifier INSTANCE = new AuctionNotifier();
    }

    public static AuctionNotifier getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public void registerObserver(Observer o) {
        observers.addIfAbsent(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {

        for (Observer observer : observers) {
            try {
                observer.update(message);
            } catch (Exception e) {
                System.err.println("Error notifying observer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void notifyNewBid(int auctionId, double newPrice, String bidderName) {

        String message = String.format(
                "NEW_BID|%d|%.2f|%s",
                auctionId,
                newPrice,
                bidderName
        );

        notifyObservers(message);
    }

    public void notifyAuctionStatusChanged(String itemName, String newStatus) {

        String message = String.format(
                "STATUS_CHANGE|%s|%s",
                itemName,
                newStatus
        );

        notifyObservers(message);
    }
    public void notifyTimeExtended(int auctionId, String newEndTime) {
        String message = String.format("TIME_EXTENDED|%d|%s", auctionId, newEndTime);
        notifyObservers(message);
    }

    public void notifyAuctionResult(int auctionId, String itemName, int winnerId, double finalPrice,
                                    double bidderBalance, double sellerBalance) {
        // AUCTION_RESULT | auctionId | itemName | winnerId | finalPrice | bidderBalance | sellerBalance
        String message = String.format("AUCTION_RESULT|%d|%s|%d|%.2f|%.2f|%.2f",
                auctionId, itemName, winnerId, finalPrice, bidderBalance, sellerBalance);
        notifyObservers(message);
    }
}
