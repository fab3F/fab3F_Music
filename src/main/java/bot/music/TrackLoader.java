package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import general.Main;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackLoader implements Runnable{

    private final String name;
    private boolean exitThread;
    private final AudioPlayerManager audioPlayerManager;
    private final LinkedBlockingQueue<MusicSong> toLoad;


    TrackLoader(AudioPlayerManager manager) {
        this.name = "TrackLoader";
        Thread thread = new Thread(this, name);
        Main.thread("Created Thread: " + thread);
        exitThread = false;
        this.audioPlayerManager = manager;
        toLoad = new LinkedBlockingQueue<>();
        thread.start();
    }

    @Override
    public void run() {
        while (!exitThread) {


            if(toLoad.isEmpty()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            } else {

                MusicSong song = this.toLoad.poll();

                if(!song.invalid && !song.isLoaded) {


                    GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(song.channel.getGuild());

                    this.audioPlayerManager.loadItemOrdered(musicManager, song.url, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack audioTrack) {
                            song.setTrackAndSetLoadedTrue(audioTrack);
                            Main.debug("LOADING AUDIO TRACK: " + song.url + " LOADED: " + audioTrack.getInfo().title);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist audioPlaylist) {
                            final List<AudioTrack> tracks = audioPlaylist.getTracks();
                            if (!tracks.isEmpty()) {

                                if (song.url.startsWith("ytsearch:")) {
                                    song.setTrackAndSetLoadedTrue(tracks.get(0));
                                    Main.debug("LOADING AUDIO TRACK: " + song.url + " LOADED: " + tracks.get(0).getInfo().title);
                                    return;
                                }

                                // YouTube Playlist wird schon im LinkConverter geladen, hierzu dürfe es nie kommen
                                song.isLoaded = false;
                                song.channel.sendMessage("Ein unerwarteter Fehler beim Laden eines Songs ist aufgetreten. Eingabe: " + song.url).queue();
                                song.invalid = true;
                                Main.error("YouTube Playlist Link somehow got into normal TrackLoader. YouTube Playlist should be loaded by LinkConverter to create a single MusicSong for each YT Playlist Song.");

                            }
                        }

                        @Override
                        public void noMatches() {
                            song.isLoaded = false;
                            song.channel.sendMessage("Keine Ergebnisse gefunden für folgende Eingabe: " + song.url).queue();
                            song.invalid = true;
                        }

                        @Override
                        public void loadFailed(FriendlyException ex) {
                            song.isLoaded = false;
                            song.channel.sendMessage("Beim Laden eines Songs ist ein Fehler aufgetreten. Falls eine Playlist abgespielt werden soll, stelle sicher, dass sie nicht auf privat gestellt ist. Eingabe: " + song.url).queue();
                            song.invalid = true;
                        }
                    });

                }
            }
        }

        Main.thread(name + " has been Stopped.");
    }

    public void stopThread() {
        exitThread = true;
    }

    public void load(MusicSong song){
        if(!toLoad.contains(song)){
            toLoad.offer(song);
            song.isInLoadingProcess = true;
        }

    }


}
