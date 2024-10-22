package bot.music;

import bot.Bot;
import bot.commands.music.QueueMusicCmd;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import general.Main;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final ConcurrentLinkedDeque<MusicSong> queue = new ConcurrentLinkedDeque<>();
    private final List<String> autoPlayedSongs = new ArrayList<>();
    public final LinkedList<String> requestedSongs = new LinkedList<>();
    private MusicSong lastPlayingSong;
    private boolean isRepeat = false;
    public boolean isAutoplay = false;
    private boolean searchingForAutoplay = false;
    public TextChannel lastUsedTextChannel;
    private final int preloaded = Integer.parseInt(Bot.instance.configWorker.getBotConfig("preloadedSongs").get(0));


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
        this.lastUsedTextChannel = song.channel;
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

        if(nextSong != null && !nextSong.isLoaded){
            return;
        }

        if(nextSong == null){
            if(skipping){
                this.isRepeat = false;
                player.startTrack(null, false);
                loadNextFewSongs();
            }
            if(isAutoplay){
                startAutoPlay();
            }
            return;
        } else if(this.autoPlayedSongs.contains(nextSong.getTrack().getInfo().title) && isAutoplay && nextSong.user.equals(Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0))){
            Main.debug("Skipped Autoplay duplicate: " + nextSong.getTrack().getInfo().title);
            nextSong(skipping);
        }

        this.isRepeat = false;
        player.startTrack(nextSong.getTrack(), false);
        trackStartedPlaying(nextSong);
        loadNextFewSongs();
    }



    public void trackStartedPlaying(MusicSong song){
        song.channel.sendMessage("Jetzt spielt: **`" + song.getTrack().getInfo().title + "`** von **`" + song.getTrack().getInfo().author + "`** [" + QueueMusicCmd.calcDuration((int)song.getTrack().getInfo().length) + "]").queue();
        this.lastPlayingSong = song;
        if(song.user.equals(Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0))){
            this.autoPlayedSongs.add(song.getTrack().getInfo().title);
        } else {
            while(this.requestedSongs.size() >= 4){
                this.requestedSongs.removeFirst();
            }
            this.requestedSongs.addLast(song.getTrack().getInfo().title);
        }
    }


    // loads the next "few" songs
    public void loadNextFewSongs(){
        for(MusicSong song : new ArrayList<>(this.queue).subList(0, Math.min(queue.size(), this.preloaded))){
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
        if(this.lastPlayingSong == null){
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

    public boolean toogleAutoPlay(){
        this.isAutoplay = !isAutoplay;
        if(!isAutoplay){
            this.autoPlayedSongs.clear();
        }
        return this.isAutoplay;
    }

    public void startAutoPlay(){
        if(this.queue.isEmpty() && !searchingForAutoplay){
            searchingForAutoplay = true;

            if(this.lastPlayingSong == null){
                Bot.instance.getPM().linkConverter.loadSimilarSongs(Bot.instance.configWorker.getServerConfig(this.lastUsedTextChannel.getGuild().getId(), "defaultautoplaysong").get(0), this.lastUsedTextChannel);
            }else{
                StringBuilder s = new StringBuilder();
                if(this.requestedSongs.isEmpty()){
                    s.append(Bot.instance.configWorker.getServerConfig(this.lastUsedTextChannel.getGuild().getId(), "defaultautoplaysong").get(0));
                }else{
                    for(String str : this.requestedSongs){
                        s.append(str).append("&.&.&");
                    }
                    s.append(this.lastPlayingSong.getTrack().getInfo().title);
                }
                Bot.instance.getPM().linkConverter.loadSimilarSongs(s.toString(), lastPlayingSong.channel);
            }
            searchingForAutoplay = false;
            loadNextFewSongs();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Main.debug("Track exception: " + exception.getMessage());
        this.lastUsedTextChannel.sendMessage("Beim Spielen eines Songs ist ein Fehler aufgetreten: **" + track.getInfo().title + "**\n" +
                "Fehlermeldung: " + exception).queue();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        Main.debug("Track stuck: " + track.getInfo().title);
    }

}