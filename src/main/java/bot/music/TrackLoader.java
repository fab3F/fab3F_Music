package bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

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
        System.out.println("[THREAD] Created Thread: " + thread);
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
                GuildMusicManager musicManager = PlayerManager.get.getGuildMusicManager(song.channel.getGuild());

                this.audioPlayerManager.loadItemOrdered(musicManager, song.url, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        song.setTrack(audioTrack);
                        song.isLoaded = true;
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        final List<AudioTrack> tracks = audioPlaylist.getTracks();
                        if(!tracks.isEmpty()){

                            if(song.url.startsWith("ytsearch:")){
                                song.setTrack(tracks.get(0));
                                song.isLoaded = true;
                                return;
                            }

                            // YouTube Playlist wird schon im LinkConverter geladen.
                            song.setTrack(tracks.get(0));
                            song.isLoaded = true;

                        }
                    }

                    @Override
                    public void noMatches() {
                        song.isLoaded = false;
                        song.channel.sendMessage("Keine Ergebnisse gefunden.").queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException ex) {
                        song.isLoaded = false;
                        song.channel.sendMessage("Beim Laden eines Songs ist ein Fehler aufgetreten. Falls eine Playlist abgespielt werden soll, stelle sicher, dass sie nicht auf privat gestellt ist.").queue();
                    }
                });

            }
        }

        System.out.println("[THREAD] " + name + " has been Stopped.");

    }

    public void stopThread() {
        exitThread = true;
    }

    public void load(MusicSong track){
        toLoad.offer(track);
    }


}
