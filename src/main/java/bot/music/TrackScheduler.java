package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final ConcurrentLinkedDeque<MusicSong> queue = new ConcurrentLinkedDeque<>();
    private MusicSong lastPlayingSong;
    private boolean isRepeat = false;
    private int aFew = 3; // how much a "few" songs is (preloaded)

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(!endReason.mayStartNext)
            return;

        if(isRepeat) {
            player.startTrack(track.makeClone(), false);
        } else {
            this.nextSong(false);
        }
    }

    public void queue(MusicSong song, boolean queueAsFirst) {
        boolean songInstantlyStartedPlaying = false;
        if(song.isLoaded && this.player.getPlayingTrack() == null && this.queue.isEmpty()){
            songInstantlyStartedPlaying = player.startTrack(song.getTrack(), true);
        }

        if(songInstantlyStartedPlaying){
            trackStartedPlaying(song);

        } else {
            if (queueAsFirst) {
                queue.addFirst(song);
            } else {
                queue.offer(song);
            }
        }

        loadNextFewSongs();
    }

    public void nextSong(boolean skipping){
        MusicSong nextSong = queue.poll();

        if(nextSong == null || !nextSong.isLoaded){
            if(skipping){
                this.isRepeat = false;
                player.startTrack(null, false);
                loadNextFewSongs();
            }
            return;
        }

        this.isRepeat = false;
        player.startTrack(nextSong.getTrack(), false);
        trackStartedPlaying(nextSong);
        loadNextFewSongs();
    }



    public void trackStartedPlaying(MusicSong song){
        int durationInSeconds = (int) song.getTrack().getInfo().length / 1000;
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;
        String length;
        if (hours > 0) {
            length = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            length = String.format("%02d:%02d", minutes, seconds);
        }

        song.channel.sendMessage("Jetzt spielt: **`" + song.getTrack().getInfo().title + "`** von **`" + song.getTrack().getInfo().author + "`** (" + length + ")").queue();
        this.lastPlayingSong = song;
    }


    // loads the next "few" songs
    public void loadNextFewSongs(){
        for(MusicSong song : new ArrayList<>(this.queue).subList(0, Math.min(queue.size(), this.aFew))){
            if(song.invalid){
                this.queue.remove(song);
            }else if(!song.isLoaded && !song.isInLoadingProcess){
                Bot.instance.getPM().trackLoader.load(song);
            }
        }

    }

    public MusicSong getNextSong(){
        return this.queue.peek();
    }

    public MusicSong getLastPlaying(){
        if(this.lastPlayingSong == null || this.player.getPlayingTrack() == null){
            return null;
        } else {
            return this.lastPlayingSong;
        }
    }

    public void clearQueue(){
        this.queue.clear();
    }

    public List<MusicSong> getQueue(){
        return new ArrayList<>(this.queue);
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void changeRepeat() {
        isRepeat = !isRepeat;
    }
}