package com.example.auctionmanagementsystem.client;

import com.example.auctionmanagementsystem.server.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public final class ApiClient {
    public static String serverUrl = "http://localhost:8080";private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private ApiClient() {}
    public static JsonObject getObject(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .timeout(Duration.ofSeconds(15))
                .GET().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        return JsonUtil.parseObject(resp.body());
    }

    public static JsonArray getArray(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .timeout(Duration.ofSeconds(15))
                .GET().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("👉 [ApiClient] GET " + path + " - Phản hồi: " + resp.body());
        return JsonUtil.parseArray(resp.body());
    }

    public static JsonObject post(String path, Map<String, Object> body) throws Exception {
        try {
            //chuyen mao thanh jso va in ra xem client chuan bi gui gi
            String json = JsonUtil.toJson(body);
            System.out.println(" [ApiClient] Đang gửi POST tới: " + serverUrl + path);
            System.out.println(" [ApiClient] Dữ liệu gửi đi: " + json);

            // dong goi request
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + path))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            // gui di va cho server tra loi lai
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("👉 [ApiClient] Nhận phản hồi (Mã trạng thái: " + resp.statusCode() + "): " + resp.body());
             // parse phan hoi tu server thanh JsonObject
            return JsonUtil.parseObject(resp.body());
        } catch (Exception e) {
            System.err.println(" [ApiClient] LỖI MẠNG HOẶC PARSE JSON KHI GỌI TỚI " + path);
            e.printStackTrace();
            throw e;
        }
    }
}
