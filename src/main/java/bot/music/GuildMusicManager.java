package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import general.Main;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager implements Runnable {

    // Thread
    private final String name;
    private boolean exitThread;

    private Guild guild;
    private int volume;

    public AudioPlayer audioPlayer;
    public TrackScheduler scheduler;

    private AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);

        this.guild = guild;
        this.volume = 100;
        this.audioPlayer.setVolume(100);

        this.guild.getAudioManager().setSendingHandler(this.sendHandler);

        // Thread
        this.name = "GuildMusicManager for " + guild.getId() + " - " + guild.getName();
        Thread thread = new Thread(this, name);
        Main.thread("Created Thread: " + thread);
        exitThread = false;
        thread.start();
    }

    public void stopManager(){

        this.exitThread = true;

        this.guild.getAudioManager().closeAudioConnection();

        this.clearQueue();
        this.audioPlayer.stopTrack();
        this.audioPlayer.destroy();
        this.audioPlayer = null;

        this.sendHandler = null;

        this.scheduler = null;

    }

    public void clearQueue(){
        this.scheduler.clearQueue();
    }

    public void setVolume(int volume){
        this.volume = volume;
        this.audioPlayer.setVolume(this.volume);
    }

    public int getVolume(){
        return this.volume;
    }


    @Override
    public void run() {
        while (!exitThread) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}


            if(this.scheduler != null){
                this.scheduler.loadNextFewSongs();
                if(this.audioPlayer.getPlayingTrack() == null && this.scheduler.getNextSong() != null && this.scheduler.getNextSong().isLoaded){
                    this.scheduler.nextSong(false);
                }

            }

        }
        Main.thread(name + " has been Stopped.");
    }

}
