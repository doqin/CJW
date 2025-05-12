package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
            int responseCode = 0;
//            URL url = new URL(String.format("https://api.henrikdev.xyz/valorant/v2/account/%s/%s/", VAL_NAME, VAL_TAG));
//
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//
//            connection.setRequestProperty("Authorization", VAL_API_KEY);
//            connection.setRequestProperty("Accept", "*/*");

//            int account_level = 0;
            String currenttierpatched = null;
            String patched_tier = null;

//            responseCode = connection.getResponseCode();
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

            URL url2 = new URL(String.format("https://api.henrikdev.xyz/valorant/v2/mmr/%s/%s/%s", VAL_REGION, VAL_NAME, VAL_TAG));

            connection = (HttpURLConnection) url2.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", VAL_API_KEY);
            connection.setRequestProperty("Accept", "*/*");

            responseCode = connection.getResponseCode();
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
                //System.out.println(current_data.toString());
                currenttierpatched = current_data.getString("currenttierpatched");
                JSONObject highest_rank = data.getJSONObject("highest_rank");
                patched_tier = highest_rank.getString("patched_tier");
//            } else {
//                System.out.println("Error getting info from API: " + responseCode);
            }

            connection.disconnect();

            Map<Object, String> responseMap = new HashMap<>();
            //responseMap.put("account_level", (account_level != 0)?String.format("%d", account_level):null);
            responseMap.put("currenttierpatched", (currenttierpatched != null)? currenttierpatched.toString():null);
            responseMap.put("patched_tier", (patched_tier != null)?patched_tier.toString():null);

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
    }
}
