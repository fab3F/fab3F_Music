package bot.music;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyWorker {

    private final String spotifyClientId;
    private final String spotifyClientSecret;
    private SpotifyApi spotifyApi;

    private final String ERROR_PREFIX = LinkConverter.ERROR_PREFIX;


    protected SpotifyWorker(String spotifyClientId, String spotifyClienSecret){
        this.spotifyClientId = spotifyClientId;
        this.spotifyClientSecret = spotifyClienSecret;
    }

    protected void close(){
        this.spotifyApi = null;
    }

    protected boolean initializeSpotify(){
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

    protected ArrayList<String> loadSpotifyLink(String link){
        link = link.replaceFirst("\\?.*$", ""); // remove parameters
        ArrayList<String> listOfTracks = new ArrayList<>();

        if(!initializeSpotify()){
            listOfTracks.add(ERROR_PREFIX + "ERROR 40: Cannot connect with Spotify API.");
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
        } else {
            listOfTracks.add(ERROR_PREFIX + "ERROR 47: Spotify link was not valid.");
            return listOfTracks;
        }

    }

    protected ArrayList<String> loadSpotifyRecommended(String name){
        ArrayList<String> a = new ArrayList<>(1);
        if(!initializeSpotify()){
            a.add(ERROR_PREFIX + "ERROR 40: Cannot connect with Spotify API.");
            return a;
        }
        String trackId = searchTrackId(name);
        if(trackId.startsWith(ERROR_PREFIX)){
            a.add(trackId);
            return a;
        }
        return getRecommended(trackId);
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

    private String searchTrackId(String query){
        SearchTracksRequest request = spotifyApi.searchTracks(query).build();
        Track track;
        try{
            track = request.execute().getItems()[0];
        }catch (IOException | ParseException | SpotifyWebApiException e){
            return ERROR_PREFIX + "ERROR 42: Spotify search track request was not successful.";
        }
        return track.getId();
    }

    private ArrayList<String> getRecommended(String trackId){
        ArrayList<String> list = new ArrayList<>();
        GetRecommendationsRequest request = spotifyApi.getRecommendations().seed_tracks(trackId).limit(30).build();
        Recommendations re;
        try {
            re = request.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            list.add(ERROR_PREFIX + "ERROR 43: Spotify recommended track request was not successful.");
            return list;
        }
        for(Track track : re.getTracks()){
            list.add(track.getName() + " " + track.getArtists()[0].getName());
        }
        if(list.isEmpty()){
            list.add(ERROR_PREFIX + "ERROR 44: Spotify recommended track request was not successful.");
        }
        return list;
    }




}
