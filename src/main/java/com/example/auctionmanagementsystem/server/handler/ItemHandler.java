package com.example.auctionmanagementsystem.server.handler;

import com.example.auctionmanagementsystem.config.DatabaseConnection;
import com.example.auctionmanagementsystem.dao.AuctionDAO;
import com.example.auctionmanagementsystem.dao.ItemDAO;
import com.example.auctionmanagementsystem.model.Item;
import com.example.auctionmanagementsystem.server.JsonUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.Connection;

public class ItemHandler extends BaseHandler {
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod();
        try {
            if (path.endsWith("/item/update") && "POST".equals(method)) {
                handleUpdate(ex);
            } else if (path.endsWith("/item/delete") && "POST".equals(method)) {
                handleDelete(ex);
            } else {
                sendJson(ex, 404, err("Not found"));
            }
        } catch (Exception e) {
            sendJson(ex, 500, err(e.getMessage()));
        }
    }
    private void handleUpdate(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int itemId = body.get("itemId").getAsInt();
        String name = body.get("name").getAsString();
        String description = body.get("description").getAsString();
        try (Connection conn = DatabaseConnection.getConnection()) {
            Item item = new ItemDAO().selectById(itemId, conn);
            if (item == null) {
                sendJson(ex, 200, err("Item not found"));
                return;
            }
            item.setName(name);
            item.setDescription(description);
            new ItemDAO().update(item, conn);
        }
        sendJson(ex, 200, ok());
    }
    private void handleDelete(HttpExchange ex) throws Exception {
        JsonObject body = JsonUtil.parseObject(readBody(ex));
        int auctionId = body.get("auctionId").getAsInt();
        int itemId = body.get("itemId").getAsInt();
        try (Connection conn = DatabaseConnection.getConnection()) {
            new AuctionDAO().delete(auctionId, conn);
            new ItemDAO().delete(itemId, conn);
        }
        sendJson(ex, 200, ok());
    }
}
