package com.example.auctionmanagementsystem.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class JsonUtil {
    private JsonUtil() {}
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();
    //chuyen object sang json string, {k:v}
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }
    //chuyen json string sang object
    public static JsonObject parseObject(String json) {
        return GSON.fromJson(json, JsonObject.class);
    }
    //chuyenr json string sang array {{k:v},{k:v},..}
    public static JsonArray parseArray(String json) {
        return GSON.fromJson(json, JsonArray.class);
    }
}
