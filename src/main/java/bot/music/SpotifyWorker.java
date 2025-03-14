package bot.music;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyWorker {

    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private SpotifyApi spotifyApi;
    private long tokenExpires;

    private final String ERROR_PREFIX = LinkConverter.ERROR_PREFIX;


    protected SpotifyWorker(String spotifyClientId, String spotifyClienSecret){
        this.spotifyClientId = spotifyClientId;
        this.spotifyClientSecret = spotifyClienSecret;
        this.tokenExpires = System.currentTimeMillis() - 999999999;
    }

    protected void close(){
        this.spotifyApi = null;
    }

    protected boolean initializedSpotify(){
        if(this.spotifyApi == null){
            this.spotifyApi = new SpotifyApi.Builder().setClientId(this.spotifyClientId).setClientSecret(this.spotifyClientSecret).build();
        }

        if (tokenExpires <= System.currentTimeMillis()) {
            ClientCredentialsRequest request = this.spotifyApi.clientCredentials().build();
            ClientCredentials creds;
            try {
                creds = request.execute();
            } catch (IOException | SpotifyWebApiException | ParseException ex) {
                this.spotifyApi = null;
                return false;
            }
            spotifyApi.setAccessToken(creds.getAccessToken());
            this.tokenExpires = System.currentTimeMillis() + (creds.getExpiresIn() * 1000) - 60000;
        }
        return true;
    }

    protected ArrayList<String> loadSpotifyLink(String link){
        link = link.replaceFirst("\\?.*$", ""); // remove parameters
        ArrayList<String> listOfTracks = new ArrayList<>();

        if(!initializedSpotify()){
            listOfTracks.add(ERROR_PREFIX + "ERROR 40: Cannot connect with Spotify API.");
            return listOfTracks;
        }

        Pattern trackPattern = Pattern.compile("track/([a-zA-Z0-9]+)");
        Matcher trackMatcher = trackPattern.matcher(link);

        Pattern playlistPattern = Pattern.compile("playlist/([a-zA-Z0-9]+)");
        Matcher playlistMatcher = playlistPattern.matcher(link);

        Pattern albumPattern = Pattern.compile("album/([a-zA-Z0-9]+)");
        Matcher albumMatcher = albumPattern.matcher(link);

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
                listOfTracks.add(ERROR_PREFIX + "ERROR 45: Spotify playlist request was not successful. The playlist may be set to private.");
                return listOfTracks;
            }
            Paging<PlaylistTrack> playlistPaging = playlist.getTracks();
            PlaylistTrack[] playlistTracks = playlistPaging.getItems();

            for (PlaylistTrack i : playlistTracks) {
                Track track = (Track) i.getTrack();
                listOfTracks.add(track.getName() + " " + track.getArtists()[0].getName());
            }
            if(listOfTracks.isEmpty()){
                listOfTracks.add(ERROR_PREFIX + "ERROR 46: No track from Spotify playlist could be loaded.");
            }
            return listOfTracks;

        } else if (albumMatcher.find()) {
            String playlistId = albumMatcher.group(1);
            GetAlbumRequest albumRequest = spotifyApi.getAlbum(playlistId).build();
            Album album;
            try{
                album = albumRequest.execute();
            }catch (IOException | ParseException | SpotifyWebApiException e){
                listOfTracks.add(ERROR_PREFIX + "ERROR 48: Spotify album request was not successful.");
                return listOfTracks;
            }
            Paging<TrackSimplified> albumPaging = album.getTracks();
            TrackSimplified[] albumTracks = albumPaging.getItems();

            for (TrackSimplified track : albumTracks) {
                listOfTracks.add(track.getName() + " " + track.getArtists()[0].getName());
            }
            if(listOfTracks.isEmpty()){
                listOfTracks.add(ERROR_PREFIX + "ERROR 49: No track from Spotify album could be loaded.");
            }
            return listOfTracks;

        } else {
            listOfTracks.add(ERROR_PREFIX + "ERROR 47: Spotify link was not valid. Able to load: Track, Playlist, Album");
            return listOfTracks;
        }

    }

    private String getSpotifyArtistAndName(String trackId) {
        GetTrackRequest trackRequest = spotifyApi.getTrack(trackId).build();
        Track track;
        try{
            track = trackRequest.execute();
        }catch (IOException | ParseException | SpotifyWebApiException e){
            return ERROR_PREFIX + "ERROR 41: Spotify track request was not successful.";
        }
        return track.getName() + " " + track.getArtists()[0].getName();
    }

}

