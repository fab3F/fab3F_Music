package bot.music;

import bot.Bot;
import general.Main;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoPlayHandler {

    private static final String apiKey = Bot.instance.configWorker.getBotConfig("lastFMkey").get(0);
    private static final String userAgent = "Music Bot/" + Main.version + " fab3F";
    private static final String base = "http://ws.audioscrobbler.com/2.0/";

    public static boolean loadTop30(TextChannel channel){
        TrackScheduler scheduler = Bot.instance.getPM().getGuildMusicManager(channel.getGuild()).scheduler;
        String searchQuery = String.format("method=chart.gettoptracks&api_key=%s&format=json&limit=30", apiKey);
        String searchUrl = base + "?" + searchQuery;
        JSONObject jsonResponse;
        try {
            jsonResponse = getJsonResponse(searchUrl);
        } catch (Exception e) {
            Main.error("Exception lastFM: " + e.getMessage());
            return false;
        }
        JSONArray trackArray = jsonResponse.getJSONObject("tracks").getJSONArray("track");
        for (int i = 0; i < trackArray.length(); i++) {
            JSONObject trackObject = trackArray.getJSONObject(i);
            String name = trackObject.getString("name");
            String artist = trackObject.getJSONObject("artist").getString("name");
            scheduler.queue(new MusicSong("ytsearch:" + artist + " - " + name + " audio", channel, Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0)), false);
        }
        return true;
    }


    private static JSONObject getJsonResponse(String urlString) throws Exception {
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
