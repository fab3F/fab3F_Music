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
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkConverter {

    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private SpotifyApi spotifyApi;

    public static final String ERROR_PREFIX = "_ERR_";
    public static final Pattern YOUTUBE_VIDEO_ID_PATTERN = Pattern.compile("(?:https?://)?(?:www\\.)?(?:youtube\\.com|youtu\\.be)/(?:.*[?&]v=|v/|embed/|watch\\?v=|.*#.*/)?([^&\\n?#]+)");

    public LinkConverter(String spotifyClientId, String spotifyClienSecret){
        this.spotifyClientId = spotifyClientId;
        this.spotifyClientSecret = spotifyClienSecret;
    }

    public void close(){
        this.spotifyApi = null;
    }


    public void addUrl(String input, SlashCommandInteractionEvent e, boolean playAsFirst){
        if(e.getGuild() == null)
            return;

        input = replaceUnallowedCharacters(input);

        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        Matcher matcher = YOUTUBE_VIDEO_ID_PATTERN.matcher(input);
        if(matcher.find()){
            input =  "https://www.youtube.com/watch?v=" + matcher.group(1);
        }

        if((input.startsWith("https") && input.contains("youtu.be/")) || (input.startsWith("https") && input.contains("spotify.link/")) ){

            input = this.expandURL(input);

            if(error(e.getHook(), input))
                return;

            Main.debug("Expanded URL: " + input);
        }


        if(input.startsWith("https") && input.contains("youtube.com/watch") && !input.contains("list=")){
            Main.debug("Loading YT Video: " + input);
            e.getHook().sendMessage("YouTube Song zur Wiedergabeliste hinzugefügt: " + input).queue();
            musicManager.scheduler.queue(new MusicSong(input, e.getChannel().asTextChannel(), e.getUser().getName()), playAsFirst);
        }



        else if(input.startsWith("https") && input.contains("youtube.com/") && input.contains("list=")){
            Main.debug("Loading YT List: " + input);
            e.getHook().sendMessage("YouTube Playlist wird geladen und zur Wiedergabeliste hinzugefügt, dies kann einige Sekunden dauern: " + input).queue();

            this.loadYouTubePlaylist(input, e.getUser(), e.getChannel().asTextChannel(), musicManager);
            // result i handeled in function
        }

        else if(input.startsWith("https") && input.contains("spotify.com/") && input.contains("/track/")){
            Main.debug("Loading Spotify Song: " + input);

            String song = this.loadSpotify(input).get(0);
            if(error(e.getHook(), song))
                return;

            musicManager.scheduler.queue(new MusicSong("ytsearch:" + song + " audio", e.getChannel().asTextChannel(), e.getUser().getName()), playAsFirst);
            e.getHook().sendMessage("Spotify Song zur Wiedergabeliste hinzugefügt: " + input).queue();
            Main.debug("Queued Spotify Song: " + input);
        }


        else if(input.startsWith("https") && input.contains("spotify.com/") && input.contains("/playlist/")){
            Main.debug("Loading Spotify List: " + input);

            e.getHook().sendMessage("Spotify Playlist wird geladen und zur Wiedergabeliste hinzugefügt, dies kann einige Sekunden dauern: " + input).queue();
            List<String> list = this.loadSpotify(input);

            if(error(e.getChannel().asTextChannel(), list.get(0)))
                return;

            for(String name : list){
                musicManager.scheduler.queue(new MusicSong("ytsearch:" + name + " audio", e.getChannel().asTextChannel(), e.getUser().getName()), false);
            }
            e.getChannel().sendMessage("Spotify Playlist wurde fertig geladen.").queue();
            Main.debug("Queued Spotify List: " + input);

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
                    return "_ERR_ERROR 20: URL cannot be converted. Request returend following response code: " + responseCode;
                }
            }
            if(redirectCount != 999)
                return "_ERR_ERROR 21: URL cannot be converted. Too many redirects: " + shortUrl;
        } catch (IOException e) {
            return "_ERR_ERROR 22: URL cannot be converted: " + shortUrl;
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
            return "_ERR_ERROR 23: URL cannot be cleaned: " + expandedUrl;
        }
    }

    private boolean initializeSpotify(){
        if(spotifyApi != null)
            return true;

        this.spotifyApi = new SpotifyApi.Builder().setClientId(this.spotifyClientId).setClientSecret(this.spotifyClientSecret).build();
        ClientCredentialsRequest request = this.spotifyApi.clientCredentials().build();
        ClientCredentials creds;
        try {
            creds = request.execute();
        } catch (IOException | SpotifyWebApiException | ParseException ex) {
            this.spotifyApi = null;
            return false;
        }
        spotifyApi.setAccessToken(creds.getAccessToken());
        return true;
    }

    private String getSpotifyArtistAndName(String trackID) {
        StringBuilder artistNameAndTrackName;
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackID).build();

        Track track;

        try{
            track = trackRequest.execute();
        }catch (IOException | ParseException | SpotifyWebApiException e){
            return "_ERR_ERROR 41: Spotify track request was not successful.";
        }

        artistNameAndTrackName = new StringBuilder(track.getName() + " - ");

        ArtistSimplified[] artists = track.getArtists();
        for(ArtistSimplified i : artists) {
            artistNameAndTrackName.append(i.getName()).append(" ");
        }

        return artistNameAndTrackName.toString();
    }

    private ArrayList<String> loadSpotify(String link){
        link = link.replaceFirst("\\?.*$", "");
        ArrayList<String> listOfTracks = new ArrayList<>();

        if(!initializeSpotify()){
            listOfTracks.add("_ERR_ERROR 40: Cannot connect with Spotify API.");
            return listOfTracks;
        }

        Pattern trackPattern = Pattern.compile("track/([a-zA-Z0-9]+)");
        Matcher trackMatcher = trackPattern.matcher(link);

        Pattern playlistPattern = Pattern.compile("playlist/([a-zA-Z0-9]+)");
        Matcher playlistMatcher = playlistPattern.matcher(link);

        if (trackMatcher.find()) {
            String trackId = trackMatcher.group(1);
            listOfTracks.add(this.getSpotifyArtistAndName(trackId));
            return listOfTracks;

        } else if (playlistMatcher.find()) {
            String playlistId = playlistMatcher.group(1);
            GetPlaylistRequest playlistRequest = spotifyApi.getPlaylist(playlistId).build();
            Playlist playlist;
            try{
                playlist = playlistRequest.execute();
            }catch (IOException | ParseException | SpotifyWebApiException e){
                listOfTracks.add("_ERR_ERROR 42: Spotify playlist request was not successful. The playlist may be set to private.");
                return listOfTracks;
            }
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                String trackID = track.getId();
                String aAndN = this.getSpotifyArtistAndName(trackID);
                if(!aAndN.startsWith(ERROR_PREFIX)){
                    listOfTracks.add(aAndN);
                }

            }
            if(listOfTracks.isEmpty()){
                listOfTracks.add("_ERR_ERROR 45: No track from Spotify playlist could be loaded.");
            }
            return listOfTracks;
        } else {
            listOfTracks.add("_ERR_ERROR 46: Spotify link was not valid.");
            return listOfTracks;
        }

    }

    public void loadSimilarSongs(String name, TextChannel channel){
        name = replaceUnallowedCharacters(name);
        List<String> l = Bot.instance.configWorker.getBotConfig("lastFMkey");
        if(l.isEmpty()){
            l.add("_ERR_ERROR 70: No lastFM API KEY");
        } else {
            String key = l.get(0);
            String userAgent = "Music Bot/" + Main.version + " fab3F";
            String base = "http://ws.audioscrobbler.com/2.0/";
            l = LastFMFinder.getSimilarSongs(name, key, userAgent, base);
        }
        TrackScheduler scheduler = Bot.instance.getPM().getGuildMusicManager(channel.getGuild()).scheduler;
        if(l.get(0).startsWith(ERROR_PREFIX) && name.length() > 25){
            loadSimilarSongs(name.substring(0, Math.min(40, name.length()) - 1), channel);
            return;
        } else if(error(channel, l.get(0))){
            if(scheduler.isAutoplay){
                scheduler.toogleAutoPlay();
            }
            return;
        }
        for(String s : l){
            scheduler.queue(new MusicSong("ytsearch:" + s + " audio", channel, Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0)), false);
        }
    }


    private void loadYouTubePlaylist(String link, User user, TextChannel channel, GuildMusicManager musicManager) {

        Bot.instance.getPM().getAudioPlayerManager().loadItemOrdered(musicManager, link, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                handleYTListLoadingResult(channel, "_ERR_ERROR 61: YouTube Playlist should be loaded but single AudioTrack was loaded.");
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                String msg;
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    for (AudioTrack track : tracks) {
                        musicManager.scheduler.queue(new MusicSong(track, channel, user.getName()), false);
                    }
                    msg = "YouTube Playlist wurde fertig geladen.";
                } else {
                    msg = "_ERR_ERROR 62: YouTube Playlist should be loaded but AudioPlaylist was empty.";
                }
                handleYTListLoadingResult(channel, msg);
            }

            @Override
            public void noMatches() {
                handleYTListLoadingResult(channel, "Keine Ergebnisse gefunden für folgende Eingabe: " + link);
            }

            @Override
            public void loadFailed(FriendlyException ex) {
                handleYTListLoadingResult(channel, "Beim Laden einer YouTube Playlist ist ein Fehler aufgetreten. Stelle sicher, dass sie nicht auf privat gestellt ist. Eingabe: " + link);
            }
        });
    }

    private void handleYTListLoadingResult(TextChannel channel, String msg) {
        if (msg.startsWith(ERROR_PREFIX)) {
            channel.sendMessage(msg.replace(ERROR_PREFIX, "")).queue();
            Main.debug("ERROR MESSAGE: " + msg);
        } else {
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


}
