package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import general.Main;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.NavigableMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkConverter extends SpotifyWorker{

    public static final String ERROR_PREFIX = "_ERR_";
    public static final Pattern YOUTUBE_VIDEO_ID_PATTERN = Pattern.compile("(?:https?://)?(?:www\\.)?(?:youtube\\.com|youtu\\.be)/(?:.*[?&]v=|v/|embed/|watch\\?v=|.*#.*/)?([^&\\n?#]+)");
    public static final Pattern YOUTUBE_PLAYLIST_ID_PATTERN = Pattern.compile("(?:https?://)?(?:www\\.)?youtube\\.com/playlist\\?list=([^&\\n?#]+)");

    public LinkConverter(String spotifyClientId, String spotifyClienSecret){
        super(spotifyClientId, spotifyClienSecret);
    }


    public void addUrl(String input, SlashCommandInteractionEvent e, boolean playAsFirst){
        if(e.getGuild() == null)
            return;

        Main.debug("Loading new Input: " + input + " replaceUnallowedCharacters --> " + (input = replaceUnallowedCharacters(input)));

        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        Matcher plMatcher = YOUTUBE_PLAYLIST_ID_PATTERN.matcher(input);
        Matcher viMatcher = YOUTUBE_VIDEO_ID_PATTERN.matcher(input);
        // Check for playlist first otherwise it will always find video
        if(plMatcher.find()){
            input = "https://www.youtube.com/playlist?list=" + plMatcher.group(1);
        }
        else if(viMatcher.find()){
            input =  "https://www.youtube.com/watch?v=" + viMatcher.group(1);
        }


        // youtu.be should never be expaned because of video pattern matcher
        if((input.startsWith("https") && input.contains("youtu.be/")) || (input.startsWith("https") && input.contains("spotify.link/")) ){

            Main.debug("Expanding URL: " + input + " EXPANDED --> " + (input = this.expandURL(input)));

            if(error(e.getHook(), input))
                return;

        }


        if(input.startsWith("https") && input.contains("youtube.com/watch") && !input.contains("list=")){
            Main.debug("Loading YT Video: " + input);
            e.getHook().sendMessage("YouTube Song zur Wiedergabeliste hinzugefügt: " + input).queue();
            musicManager.scheduler.queue(new MusicSong(input, e.getChannel().asTextChannel(), e.getUser().getName()), playAsFirst);
        }


        else if(input.startsWith("https") && input.contains("youtube.com/playlist") && input.contains("list=")){
            Main.debug("Loading YT List: " + input);
            e.getHook().sendMessage("YouTube Playlist wird geladen und zur Wiedergabeliste hinzugefügt, dies kann einige Sekunden dauern: " + input).queue();

            this.loadYouTubePlaylist(input, e.getUser().getName(), e.getChannel().asTextChannel(), musicManager, false);
            // result is handeled in function
        }


        else if(input.startsWith("https") && input.contains("spotify.com/") && input.contains("/track/")){
            Main.debug("Loading Spotify Song: " + input);

            String song = this.loadSpotifyLink(input).get(0);
            if(error(e.getHook(), song))
                return;

            musicManager.scheduler.queue(new MusicSong("ytsearch:" + song + " audio", e.getChannel().asTextChannel(), e.getUser().getName()), playAsFirst);
            e.getHook().sendMessage("Spotify Song zur Wiedergabeliste hinzugefügt: " + input).queue();
        }


        else if(input.startsWith("https") && input.contains("spotify.com/") && input.contains("/playlist/")){
            Main.debug("Loading Spotify List: " + input);

            e.getHook().sendMessage("Spotify Playlist wird geladen und zur Wiedergabeliste hinzugefügt, dies kann einige Sekunden dauern: " + input).queue();
            List<String> list = this.loadSpotifyLink(input);

            if(error(e.getChannel().asTextChannel(), list.get(0)))
                return;

            for(String name : list){
                musicManager.scheduler.queue(new MusicSong("ytsearch:" + name + " audio", e.getChannel().asTextChannel(), e.getUser().getName()), false);
            }
            e.getChannel().sendMessage("Spotify Playlist wurde fertig geladen.").queue();
        }

        else{
            Main.debug("Loading YT Search: " + input);
            musicManager.scheduler.queue(new MusicSong("ytsearch:" + input + " audio", e.getChannel().asTextChannel(), e.getUser().getName()), playAsFirst);
            e.getHook().sendMessage("Song zur Wiedergabeliste hinzugefügt: " + input).queue();
        }
    }

    public String expandURL(String shortUrl) {
        shortUrl = shortUrl.replaceFirst("\\?.*$", "");
        final int MAX_REDIRECTS = 4;  // Maximale Anzahl von Weiterleitungen
        int redirectCount = 0;
        HttpURLConnection connection = null;
        String expandedUrl = "";
        try {
            while (redirectCount < MAX_REDIRECTS) {
                redirectCount++;
                URL url = new URL(shortUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                        responseCode == 307) {
                    shortUrl = connection.getHeaderField("Location");
                } else if (responseCode == HttpURLConnection.HTTP_OK) {
                    expandedUrl = shortUrl;
                    redirectCount = 999;
                } else {
                    return ERROR_PREFIX + "ERROR 20: URL cannot be converted. Request returend following response code: " + responseCode;
                }
            }
            if(redirectCount != 999)
                return ERROR_PREFIX + "ERROR 21: URL cannot be converted. Too many redirects: " + shortUrl;
        } catch (IOException e) {
            return ERROR_PREFIX + "ERROR 22: URL cannot be converted: " + shortUrl;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        // Remove parameters and keep only the 'v' parameter if it exists
        try {
            URI uri = new URI(expandedUrl);
            String query = uri.getQuery();
            String videoId = null;
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("v")) {
                        videoId = keyValue[1];
                        break;
                    }
                }
            }

            StringBuilder cleanedUrl = new StringBuilder();
            cleanedUrl.append(uri.getScheme())
                    .append("://")
                    .append(uri.getHost())
                    .append(uri.getPath());

            if (videoId != null) {
                cleanedUrl.append("?v=").append(videoId);
            }

            return cleanedUrl.toString();
        } catch (Exception ex) {
            return ERROR_PREFIX + "ERROR 23: URL cannot be cleaned: " + expandedUrl;
        }
    }


    public void loadYouTubePlaylist(String link, String username, TextChannel channel, GuildMusicManager musicManager, boolean isAutoplayRequest) {

        Bot.instance.getPM().getAudioPlayerManager().loadItemOrdered(musicManager, link, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                handleYTListLoadingResult(channel, ERROR_PREFIX + "ERROR 61: YouTube Playlist should be loaded but single AudioTrack was loaded.");
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                String msg;
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    if(isAutoplayRequest)
                        tracks.remove(0);
                    for (AudioTrack track : tracks) {
                        musicManager.scheduler.queue(new MusicSong(track, channel, username), false);
                    }
                    msg = isAutoplayRequest ? "_AUTO" : "YouTube Playlist wurde fertig geladen.";
                } else {
                    msg = ERROR_PREFIX + "ERROR 62: YouTube Playlist should be loaded but AudioPlaylist was empty.";
                }
                handleYTListLoadingResult(channel, msg);
            }

            @Override
            public void noMatches() {
                handleYTListLoadingResult(channel, "Keine Ergebnisse gefunden für folgende Eingabe: " + link);
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                if(isAutoplayRequest){
                    musicManager.scheduler.isAutoplay = false;
                    handleYTListLoadingResult(channel, ERROR_PREFIX + "ERROR 63: Kein AutoPlay für diesen Song verfügbar. AutoPlay wurde deaktiviert. Eingabe: " + link);
                }
                else
                    handleYTListLoadingResult(channel, "Beim Laden einer YouTube Playlist ist ein Fehler aufgetreten. Stelle sicher, dass sie nicht auf privat gestellt ist. Eingabe: " + link);
            }
        });
    }

    private void handleYTListLoadingResult(TextChannel channel, String msg) {
        if (msg.startsWith(ERROR_PREFIX)) {
            channel.sendMessage(msg.replace(ERROR_PREFIX, "")).queue();
            Main.debug("ERROR MESSAGE: " + msg);
        } else if(!msg.startsWith("_AUTO")) {
            channel.sendMessage(msg).queue();
        }
    }

    private boolean error(InteractionHook hook, String msg) {
        if (msg.startsWith(ERROR_PREFIX)) {
            hook.sendMessage(msg.replace(ERROR_PREFIX, "")).queue();
            Main.debug("ERROR MESSAGE: " + msg);
            return true;
        }
        return false;
    }

    private boolean error(TextChannel channel, String msg) {
        if (msg.startsWith(ERROR_PREFIX)) {
            channel.sendMessage(msg.replace(ERROR_PREFIX, "")).queue();
            Main.debug("ERROR MESSAGE: " + msg);
            return true;
        }
        return false;
    }

    private String replaceUnallowedCharacters(String s) {
        String unallowedCharacters = Bot.instance.configWorker.getBotConfig("unallowedCharactersMusicList").get(0);
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (unallowedCharacters.indexOf(c) == -1) {
                result.append(c);
            }
        }
        return result.toString();
    }

    // replaces brackets, unallowed symbols
    private String repairTextSearch(String search){
        return replaceUnallowedCharacters(search)
                .replaceAll("\\([^)]*\\)", "")
                .replaceAll("\\[[^]]*]", "")
                .replaceAll(" - ", " ")
                .replaceAll(" & ", " ")
                .replaceAll("\\|", "")
                .replaceAll("#", "");
    }


}
