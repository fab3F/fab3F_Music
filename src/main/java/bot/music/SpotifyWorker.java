package bot.music;

import com.neovisionaries.i18n.CountryCode;
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

    protected ArrayList<String> loadSpotifyRecommended(String name, int popularity){
        ArrayList<String> a = new ArrayList<>(1);
        if(!initializedSpotify()){
            a.add(ERROR_PREFIX + "ERROR 40: Cannot connect with Spotify API.");
            return a;
        }
        StringBuilder tracks = new StringBuilder();
        for(String song : name.split("&.&.&")) {
            SearchTracksRequest request = spotifyApi.searchTracks(song).build();
            Track track;
            try {
                track = request.execute().getItems()[0];
            } catch (IOException | ParseException | SpotifyWebApiException e) {
                a.add(ERROR_PREFIX + "ERROR 42: Spotify search track request was not successful.");
                return a;
            }
            tracks.append(track.getId()).append(",");
        }
        return getRecommended(tracks.toString(), popularity);
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

    private ArrayList<String> getRecommended(String tracks, int popularity){
        //String artistIds = track.getArtists()[0].getId() + (track.getArtists().length > 1 ? "," + track.getArtists()[1].getId() : "");
        ArrayList<String> list = new ArrayList<>();
        GetRecommendationsRequest request = spotifyApi.getRecommendations()
                .seed_tracks(tracks)
                //.seed_artists(artistIds)
                .min_popularity(popularity)
                .market(CountryCode.DE)
                .limit(30).build();
        Recommendations re;
        try {
            re = request.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            list.add(ERROR_PREFIX + "ERROR 43: Spotify recommended track request was not successful.");
            return list;
        }
        for(Track tr : re.getTracks()){
            list.add(tr.getName() + " " + tr.getArtists()[0].getName());
        }
        if(list.isEmpty()){
            list.add(ERROR_PREFIX + "ERROR 44: Spotify recommended track request was not successful.");
        }
        return list;
    }

}


