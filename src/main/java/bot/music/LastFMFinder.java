package bot.music;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LastFMFinder {


    public static ArrayList<String> getSimilarSongs(String track, String apiKey, String userAgent, String baseUrl) {
        ArrayList<String> list = new ArrayList<>();
        try {
            String artist = getArtistFromTrack(track, apiKey, userAgent, baseUrl);
            if (artist != null) {
                list =  fetchSimilarTracks(artist, track, apiKey, userAgent, baseUrl);
                if(!list.isEmpty()){
                    return list;
                }
            }
            list.add("_ERR_ERROR 71: No results found for the given track name: " + track);

        } catch (Exception e) {
            list.clear();
            list.add("_ERR_ERROR 72: An exception occurred with the following lastFM request: " + track + " - The exception: " + e.getMessage());
        }
        return list;
    }

    private static String getArtistFromTrack(String track, String apiKey, String userAgent, String baseUrl) throws Exception {
        String searchQuery = String.format("method=track.search&track=%s&api_key=%s&format=json",
                URLEncoder.encode(track, StandardCharsets.UTF_8), apiKey);
        String searchUrl = baseUrl + "?" + searchQuery;

        JSONObject searchResponse = getJsonResponse(searchUrl, userAgent);
        JSONArray trackArray = searchResponse.getJSONObject("results")
                .getJSONObject("trackmatches")
                .getJSONArray("track");

        if (!trackArray.isEmpty()) {
            return trackArray.getJSONObject(0).getString("artist");
        }
        return null;
    }

    private static ArrayList<String> fetchSimilarTracks(String artist, String track, String apiKey, String userAgent, String baseUrl) throws Exception {
        ArrayList<String> list = new ArrayList<>();
        String query = String.format("method=track.getSimilar&artist=%s&track=%s&api_key=%s&format=json",
                URLEncoder.encode(artist, StandardCharsets.UTF_8), URLEncoder.encode(track, StandardCharsets.UTF_8), apiKey);
        String url = baseUrl + "?" + query;

        JSONObject jsonResponse = getJsonResponse(url, userAgent);
        JSONArray similarTrackArray = jsonResponse.getJSONObject("similartracks").getJSONArray("track");

        for (int i = 0; i < similarTrackArray.length(); i++) {
            JSONObject similarTrackObject = similarTrackArray.getJSONObject(i);
            String similarTrackName = similarTrackObject.getString("name");
            String similarTrackArtist = similarTrackObject.getJSONObject("artist").getString("name");
            list.add(similarTrackArtist + " - " + similarTrackName);
        }
        return list;
    }

    private static JSONObject getJsonResponse(String urlString, String userAgent) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", userAgent);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();

        return new JSONObject(content.toString());
    }


}
