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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class TrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final ConcurrentLinkedDeque<MusicSong> queue = new ConcurrentLinkedDeque<>();
    private final List<MusicSong> lastPlayedSongs = new ArrayList<>();
    private boolean isRepeat = false;
    public boolean isAutoplay = false;
    private boolean searchingForAutoplay = false;
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
        } else if(!this.lastPlayedSongs.isEmpty() && this.lastPlayedSongs.stream().map(song -> song.getTrack().getInfo().title).toList().contains(nextSong.getTrack().getInfo().title) && nextSong.user.equals(Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0))){
            Main.debug("Skipped Autoplay duplicate: " + nextSong.getTrack().getInfo().title);
            nextSong(skipping);
            return;
        }

        this.isRepeat = false;
        player.startTrack(nextSong.getTrack(), false);
        trackStartedPlaying(nextSong);
        loadNextFewSongs();
    }



    public void trackStartedPlaying(MusicSong song){
        song.channel.sendMessage("Jetzt spielt: **`" + song.getTrack().getInfo().title + "`** von **`" + song.getTrack().getInfo().author + "`** [" + QueueMusicCmd.calcDuration((int)song.getTrack().getInfo().length) + "]").queue();
        this.lastPlayedSongs.add(song);

        // Loundess normalization
        List<String> l = Bot.instance.configWorker.getServerConfig(song.channel.getGuild().getId(), "volumenormalization");
        if(!l.isEmpty() && l.get(0).equalsIgnoreCase("true")) {
            int defaultvolume = Integer.parseInt(Bot.instance.configWorker.getServerConfig(song.channel.getGuild().getId(), "defaultvolume").get(0));
            String id = song.getTrack().getInfo().uri.replaceAll("^(?:https?://)?(?:www\\.)?(?:youtube\\.com/.*v=|youtu\\.be/)([a-zA-Z0-9_-]{11}).*$", "$1");
            double multiplier = LoudnessHandler.calculateVolumeMultiplierV2(id);
            int volume = (int) (defaultvolume * multiplier);
            this.player.setVolume(volume);
            song.channel.sendMessage("New volume: " + volume).queue();
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

    public MusicSong getLastPlaying(MusicSong... addLast){
        if(addLast.length > 0){
            this.lastPlayedSongs.add(addLast[0]);
        }
        if(this.lastPlayedSongs.isEmpty()){
            return null;
        } else {
            return this.lastPlayedSongs.get(lastPlayedSongs.size()-1);
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
        return this.isAutoplay;
    }

    public void startAutoPlay(){
        if(this.queue.isEmpty() && !searchingForAutoplay){
            searchingForAutoplay = true;

            if(this.lastPlayedSongs.get(this.lastPlayedSongs.size()-1).url.equals("STARTDEFAULTAUTOPLAY")){
                Bot.instance.getPM().linkConverter.loadSimilarSongs(Bot.instance.configWorker.getServerConfig(this.lastPlayedSongs.get(lastPlayedSongs.size()-1).channel.getGuild().getId(), "defaultautoplaysong").get(0), this.lastPlayedSongs.get(lastPlayedSongs.size()-1).channel);
            }else{
                String vId = this.lastPlayedSongs.get(lastPlayedSongs.size()-1).getTrack().getInfo().uri.replaceAll("^(?:https?://)?(?:www\\.)?(?:youtube\\.com/.*v=|youtu\\.be/)([a-zA-Z0-9_-]{11}).*$", "$1");
                Bot.instance.getPM().linkConverter.loadYouTubePlaylist("https://www.youtube.com/watch?v=" + vId + "&list=RD" + vId, Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0), this.lastPlayedSongs.get(lastPlayedSongs.size()-1).channel, Bot.instance.getPM().getGuildMusicManager(this.lastPlayedSongs.get(lastPlayedSongs.size()-1).channel.getGuild()), true);
            }
            searchingForAutoplay = false;
            loadNextFewSongs();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Main.debug("Track exception: " + exception.getMessage());
        if(this.lastPlayedSongs.isEmpty())
            return;
        this.lastPlayedSongs.get(lastPlayedSongs.size()-1).channel.sendMessage("Beim Spielen eines Songs ist ein Fehler aufgetreten: **" + track.getInfo().title + "**\n" +
                "Fehlermeldung: " + exception).queue();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        Main.debug("Track stuck: " + track.getInfo().title);
    }

}