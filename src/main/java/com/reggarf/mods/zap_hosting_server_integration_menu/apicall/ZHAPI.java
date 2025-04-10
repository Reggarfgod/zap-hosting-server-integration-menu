package com.reggarf.mods.zap_hosting_server_integration_menu.apicall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ZHAPI {
    public static CompletableFuture<List<String>> fetchModpacks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("apiurl"); // Adjust if needed
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                JsonArray modpackArray = JsonParser.parseString(responseBuilder.toString()).getAsJsonArray();
                List<String> modpacks = new ArrayList<>();
                for (JsonElement element : modpackArray) {
                    JsonObject obj = element.getAsJsonObject();
                    String name = obj.get("name").getAsString();
                    String version = obj.get("version").getAsString();
                    modpacks.add(name + " " + version);
                }

                return modpacks;

            } catch (Exception e) {
                e.printStackTrace();
                return List.of("Failed to load modpacks");
            }
        });
    }
}
