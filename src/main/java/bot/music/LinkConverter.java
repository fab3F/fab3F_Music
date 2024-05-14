package bot.music;

import bot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkConverter {

    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private SpotifyApi spotifyApi;

    private final String ERROR_PREFIX = "_ERR_ERROR";

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

        if(Bot.instance.debug)
            System.out.println("[DEBUG] Loading: " + input);

        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        if((input.startsWith("https") && input.contains("youtu.be/")) || (input.startsWith("https") && input.contains("spotify.link/")) ){

            input = this.expandURL(input);

            if(Bot.instance.debug)
                System.out.println("[DEBUG] Expanding URL: " + input);

            if(input.startsWith(ERROR_PREFIX)){
                e.getHook().sendMessage(input.replaceAll("_ERR_", "")).queue();
                if(Bot.instance.debug)
                    System.out.println("[DEBUG] ERROR MESSAGE: " + input);
                return;
            }

        }


        if(input.startsWith("https") && input.contains("youtube.com/watch") && !input.contains("list=")){
            if(Bot.instance.debug)
                System.out.println("[DEBUG] Loading YT Video: " + input);
            e.getHook().sendMessage("YouTube Song zur Wiedergabeliste hinzugefügt: " + input).queue();
            musicManager.scheduler.queue(new MusicSong(input, e.getChannel().asTextChannel(), e.getUser()), playAsFirst);
            return;
        }



        else if(input.startsWith("https") && input.contains("youtube.com/") && input.contains("list=")){
            if(Bot.instance.debug)
                System.out.println("[DEBUG] Loading YT List: " + input);
            e.getHook().sendMessage("YouTube Playlist ist noch in Arbeit").queue();
            return;
            // Sonderfall: hier werden die Songs schon im Link Converter geladen

        }

        else if(input.startsWith("https") && input.contains("spotify.com/") && input.contains("/track/")){
            if(Bot.instance.debug)
                System.out.println("[DEBUG] Loading Spotify Song: " + input);

            String song = this.loadSpotify(input).get(0);
            if(song.startsWith(ERROR_PREFIX)){
                e.getHook().sendMessage(song.replaceAll("_ERR_", "")).queue();
                if(Bot.instance.debug)
                    System.out.println("[DEBUG] ERROR MESSAGE: " + song);
                return;
            }

            musicManager.scheduler.queue(new MusicSong("ytsearch:" + song + " audio", e.getChannel().asTextChannel(), e.getUser()), playAsFirst);
            e.getHook().sendMessage("Spotify Song zur Wiedergabeliste hinzugefügt: " + input).queue();

            if(Bot.instance.debug)
                System.out.println("[DEBUG] Queued Spotify Song: " + input);

            return;
        }


        else if(input.startsWith("https") && input.contains("spotify.com/") && input.contains("/playlist/")){
            if(Bot.instance.debug)
                System.out.println("[DEBUG] Loading Spotify List: " + input);

            e.getHook().sendMessage("Spotify Playlist wird geladen und zur Wiedergabeliste hinzugefügt, dies kann einige Sekunden dauern: " + input).queue();
            List<String> list = this.loadSpotify(input);


            if(list.get(0).startsWith(ERROR_PREFIX)){
                e.getChannel().sendMessage(list.get(0).replaceAll("_ERR_", "")).queue();
                if(Bot.instance.debug)
                    System.out.println("[DEBUG] ERROR MESSAGE: " + list.get(0));
                return;
            }

            for(String name : list){
                musicManager.scheduler.queue(new MusicSong("ytsearch:" + name + " audio", e.getChannel().asTextChannel(), e.getUser()), false);
            }
            e.getChannel().sendMessage("Spotify Playlist wurde fertig geladen.").queue();

            if(Bot.instance.debug)
                System.out.println("[DEBUG] Queued Spotify List: " + input);

            return;
        }

        else{
            if(Bot.instance.debug)
                System.out.println("[DEBUG] Loading YT Search: " + input);
            musicManager.scheduler.queue(new MusicSong("ytsearch:" + input + " audio", e.getChannel().asTextChannel(), e.getUser()), playAsFirst);
            e.getHook().sendMessage("Song zur Wiedergabeliste hinzugefügt: " + input).queue();
            return;
        }
    }

    public String expandURL(String shortenedUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(shortenedUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String expandedUrl = connection.getHeaderField("Location");
                if (expandedUrl != null) {
                    return expandedUrl;
                }
            }
            return shortenedUrl;
        } catch (IOException ex){
            return "_ERR_ERROR 20: URL cant be converted.";
        }

    }

    private boolean initializeSpotify(){
        if(spotifyApi != null)
            return true;

        this.spotifyApi = new SpotifyApi.Builder().setClientId(this.spotifyClientId).setClientSecret(this.spotifyClientSecret).build();

        ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(spotifyApi.getClientId(), spotifyApi.getClientSecret());
        ClientCredentials creds;
        try {
            creds = request.grant_type("client_credentials").build().execute();
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
            return "_ERR_ERROR 41: Spotify Track request was not successful.";
        }

        artistNameAndTrackName = new StringBuilder(track.getName() + " - ");

        ArtistSimplified[] artists = track.getArtists();
        for(ArtistSimplified i : artists) {
            artistNameAndTrackName.append(i.getName()).append(" ");
        }

        return artistNameAndTrackName.toString();
    }

    private ArrayList<String> loadSpotify(String link){
        ArrayList<String> listOfTracks = new ArrayList<>();

        if(!initializeSpotify()){
            listOfTracks.add("_ERR_ERROR 40: Cant connect with Spotify API.");
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
                listOfTracks.add("_ERR_ERROR 42: Spotify Playlist request was not successful.");
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
                listOfTracks.add("_ERR_ERROR 45: No Track from Spotify Playlist could be loaded.");
            }
            return listOfTracks;
        } else {
            listOfTracks.add("_ERR_ERROR 46: Spotify Link was not valid.");
            return listOfTracks;
        }


    }


}
