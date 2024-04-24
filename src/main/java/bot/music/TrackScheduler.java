package bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.ConcurrentLinkedDeque;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final ConcurrentLinkedDeque<MusicSong> queue = new ConcurrentLinkedDeque<>();
    private boolean isRepeat = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(isRepeat) {
            player.startTrack(track.makeClone(), false);
        } else {
            MusicSong next = queue.peek();
            if(next == null)
                return;

            if(!next.isLoaded)
                return;

            if(next.amountOfTracks() <= 1)
                queue.poll();

            if(next.amountOfTracks() > 0){
                player.startTrack(next.getNextTrack(), false);
                sendPlayingMessage(next);
            }

        }
    }

    public void queue(MusicSong track, boolean queueAsFirst) {
        boolean songInstantlyStartedPlaying = false;
        if(track.isLoaded && track.amountOfTracks() > 0){
            songInstantlyStartedPlaying = player.startTrack(track.getNextTrack(), true);
        }

        if(songInstantlyStartedPlaying){
            sendPlayingMessage(track);
        }
        if(track.amountOfTracks() > 0) {
            if (queueAsFirst) {
                queue.addFirst(track);
            } else {
                queue.offer(track);
            }
            loadNextFewSongs();
        }

    }

    public void sendPlayingMessage(MusicSong track){
        track.channel.sendMessage("").queue();

    }

    public void loadNextFewSongs(){
        int amount = Math.min(queue.size(), 3);
        if(amount == 0){
            return;
        }
        int i = 0;
        for(MusicSong song : queue){
            if(i >= amount)
                return;
            if(!song.isLoaded){
                PlayerManager.get.trackLoader.load(song);
            }
            i++;
        }

    }


    public boolean isRepeat() {
        return isRepeat;
    }

    public void changeRepeat() {
        isRepeat = !isRepeat;
    }
}