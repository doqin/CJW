package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;

@RestController
@RequestMapping("/api")
public class ValFetcherController {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .ignoreIfMissing()
            .load();

    private static final String VAL_API_KEY = dotenv.get("VAL_API_KEY");
    private static final String VAL_REGION = dotenv.get("VAL_REGION");
    private static final String VAL_NAME = dotenv.get("VAL_NAME");
    private static final String VAL_TAG = dotenv.get("VAL_TAG");

    @GetMapping("/val-fetcher")
    public static ResponseEntity<Map<Object, String>> requestInfoAccess() {
//        System.out.println("Fetching VALORANT data...");
        try {
            HttpURLConnection connection;
            int responseCode;
            Map<Object, String> responseMap = new HashMap<>();
//            URI uri1 = new URI(String.format("https://api.henrikdev.xyz/valorant/v2/account/%s/%s/", VAL_NAME, VAL_TAG));
//
//            connection = (HttpURLConnection) uri1.toURL().openConnection();
//            connection.setRequestMethod("GET");
//
//            connection.setRequestProperty("Authorization", VAL_API_KEY);
//            connection.setRequestProperty("Accept", "*/*");
//
//            int account_level = 0;
            String currenttierpatched = null;
            String patched_tier = null;
            String currentimg_small = null;

//            responseCode = connection.getResponseCode();
//            responseMap.put("response_code_1", String.valueOf(responseCode));
//
//            if (responseCode == 200) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                StringBuilder response = new StringBuilder();
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    response.append(line);
//                }
//                reader.close();
//
//                JSONObject object = new JSONObject(response.toString());
//                JSONObject data = object.getJSONObject("data");
//                System.out.println("fetch");
//                account_level = data.getInt("account_level");
//            } else {
//                System.out.println("Error getting info from API: " + responseCode);
//            }
//
//            connection.disconnect();

            URI uri2 = new URI(String.format("https://api.henrikdev.xyz/valorant/v2/mmr/%s/%s/%s", VAL_REGION, VAL_NAME, VAL_TAG));

            connection = (HttpURLConnection) uri2.toURL().openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", VAL_API_KEY);
            connection.setRequestProperty("Accept", "*/*");

            responseCode = connection.getResponseCode();
            responseMap.put("response_code_2", String.valueOf(responseCode));

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject object = new JSONObject(response.toString());
                JSONObject data = object.getJSONObject("data");
                JSONObject current_data = data.getJSONObject("current_data");
//                System.out.println(current_data.toString());
                currenttierpatched = current_data.getString("currenttierpatched");
                JSONObject images = current_data.getJSONObject("images");
                currentimg_small = images.getString("small");
                System.out.println(currentimg_small);
                JSONObject highest_rank = data.getJSONObject("highest_rank");
                patched_tier = highest_rank.getString("patched_tier");
//            } else {
//                System.out.println("Error getting info from API: " + responseCode);
            }

            connection.disconnect();

            responseMap.put("region", VAL_REGION);
            responseMap.put("name", VAL_NAME);
            responseMap.put("tag", VAL_TAG);
//            responseMap.put("account_level", (account_level != 0)?String.format("%d", account_level):null);
            responseMap.put("currenttierpatched", currenttierpatched);
            responseMap.put("currentimg_small", currentimg_small);
            responseMap.put("patched_tier", patched_tier);

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
    }
}
