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
    public static ResponseEntity<Map<String, String>> requestInfoAccess() {
        try {
            String urlString = String.format("https://api.henrikdev.xyz/valorant/v1/account/%s/%s/", VAL_NAME, VAL_TAG);
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", VAL_API_KEY);
            connection.setRequestProperty("Accept", "*/*");

            JSONObject account_level = null;
            JSONObject currenttier_patched = null;
            JSONObject patched_tier = null;

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject object = new JSONObject(response.toString());
                JSONArray data = object.getJSONArray("data");
                account_level = data.getJSONObject(2);
//            } else {
//                System.out.println("Error getting info from API: " + responseCode);
            }

            connection.disconnect();

            String urlString2 = String.format("https://api.henrikdev.xyz/valorant/v2/mmr/%s/%s/%s", VAL_REGION, VAL_NAME, VAL_TAG);
            URL url2 = new URL(urlString2);

            HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();
            connection2.setRequestMethod("GET");

            connection2.setRequestProperty("Authorization", VAL_API_KEY);
            connection2.setRequestProperty("Accept", "*/*");

            responseCode = connection2.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject object = new JSONObject(response.toString());
                JSONArray data = object.getJSONArray("data");
                JSONObject current_data = data.getJSONObject(2);
                currenttier_patched = current_data.getJSONObject("currenttier_patched");
                JSONObject highest_rank = data.getJSONObject(3);
                patched_tier = highest_rank.getJSONObject("patched_tier");
//            } else {
//                System.out.println("Error getting info from API: " + responseCode);
            }

            connection2.disconnect();

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("account_level", (account_level != null)?account_level.toString():null);
            responseMap.put("currenttier_patched", (currenttier_patched != null)?currenttier_patched.toString():null);
            responseMap.put("patched_tier", (patched_tier != null)?patched_tier.toString():null);

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal Server Error"));
        }
    }
}
