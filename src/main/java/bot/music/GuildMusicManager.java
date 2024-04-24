package bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;

public class GuildMusicManager {

    public AudioPlayer audioPlayer;
    public TrackScheduler scheduler;

    public LinkConverter linkConverter;

    private AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager){
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
        this.linkConverter = new LinkConverter();
    }

    public static void stopEverything(GuildMusicManager m, Guild g){
        m.scheduler.ListToQueue = new ArrayList<>();
        m.scheduler.queue.clear();
        m.scheduler.audioPlayer.stopTrack();
        m.scheduler.nextTrack(true);
        m.scheduler.audioPlayer.stopTrack();
        m.audioPlayer.setPaused(false);
        m.scheduler.repeating = false;
        m.scheduler.loadedSongs = 0;
        m.scheduler.ParallelList.clear();
        m.linkConverter.stopEverything();


        final AudioManager audioManager = g.getAudioManager();
        audioManager.closeAudioConnection();

        PlayerManager.getINSTANCE().musicManagers.remove(g.getIdLong());

        m.audioPlayer = null;
        m.scheduler = null;
        m.sendHandler = null;
        m.linkConverter = null;

        m = null;
    }

    public static void clearQueue(GuildMusicManager m){
        m.scheduler.queue.clear();
        m.scheduler.ParallelList.clear();
        m.scheduler.ListToQueue = new ArrayList<>();

        if(m.audioPlayer.getPlayingTrack() != null)
            m.scheduler.loadedSongs = 1;
        else
            m.scheduler.loadedSongs = 0;
    }

    public AudioPlayerSendHandler getSendHandler(){
        return this.sendHandler;
   }
}
