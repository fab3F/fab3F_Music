package bot.music;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import general.Main;

import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;

public class LoudnessHandler {

    private static final String API_URL = "https://www.youtube.com/youtubei/v1/player?key=";
    private static final String API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";

    // Stärke des Multiplikator, je größer desto stärker
    public static double scale = 0.1;

    public static double calculateVolumeMultiplier(String videoId){
        double loudness = getLoudness(videoId);
        double x_ = Math.abs(loudness);

        // Range Multiplier: Wenn loudness zwischen -4 und 4 wird sie nur sehr wenig verändert
        double rangeMtpl = Math.abs(Math.tanh(0.00001 * Math.pow(loudness, 7)));

        double rawMultiplier = 1 - (Math.signum(loudness) * (1 - Math.pow(Math.E, -scale * x_)) * rangeMtpl);

        // Clamp the multiplier to avoid extreme volumes
        double minMultiplier = 0.5; // Minimum volume multiplier
        double maxMultiplier = 2.5; // Maximum volume multiplier
        return Math.min(maxMultiplier, Math.max(minMultiplier, rawMultiplier));
    }

    public static double calculateVolumeMultiplierV2(String videoId){
        double loudness = getLoudness(videoId);
        double rawMultiplier = Math.pow(10, -loudness/20);
        double minMultiplier = 0.65; // Minimum volume multiplier
        double maxMultiplier = 2.5; // Maximum volume multiplier
        return Math.min(maxMultiplier, Math.max(minMultiplier, rawMultiplier));
    }


    private static double getLoudness(String videoId) {

        double loudness = 0;
        try {
            URL url = new URL(API_URL + API_KEY);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"videoId\":\"" + videoId + "\","
                    + "\"context\": {"
                    + "  \"client\": {"
                    + "    \"clientName\": \"WEB\","
                    + "    \"clientVersion\": \"2.20210909.00.00\""
                    + "  }"
                    + "}"
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }


            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                String jsonResponse = scanner.useDelimiter("\\A").next();
                loudness = getPreferencedInfo(videoId, jsonResponse);
            }

        } catch (Exception e) {
            Main.error("Exception when trying to GET Loudness Info for Video: " + videoId + "\n" + e.getMessage());
        }
        return loudness;
    }


    private static double getPreferencedInfo(String videoId, String response){
        try {
            JsonObject jsonObject = JsonParser.object().from(new StringReader(response));
            JsonObject streamingData = jsonObject.getObject("streamingData");
            JsonArray adaptiveFormats = streamingData.getArray("adaptiveFormats");
            HashMap<Integer, Double> map = new HashMap<>();
            for (int i = 0; i < adaptiveFormats.size(); i++) {
                JsonObject format = adaptiveFormats.getObject(i);
                String mimeType = format.getString("mimeType");
                if (mimeType.startsWith("audio/")) {
                    if (format.has("loudnessDb")) {
                        map.put(format.getInt("itag"), format.getDouble("loudnessDb"));
                    }
                }
            }
            if(map.containsKey(140))
                return map.get(140);
            else if (map.containsKey(251)) {
                return map.get(251);
            } else if (!map.isEmpty())
                return map.values().iterator().next();
        } catch (JsonParserException e) {
            Main.error("Exception when trying to PARSE Loudness Info for Video: " + videoId + "\n" + e.getMessage());
        }
        return 0.0;
    }


}
