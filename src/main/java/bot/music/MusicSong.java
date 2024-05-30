package bot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;


public class MusicSong {

    public boolean isLoaded = false;
    public boolean invalid = false;
    public boolean isInLoadingProcess = false;


    public String url;
    public TextChannel channel;
    public String user;
    private AudioTrack audioTrack;

    public MusicSong(String url, TextChannel textChannel, String user){
        this.url = url;
        this.channel = textChannel;
        this.user = user;
        this.audioTrack = null;
    }

    public MusicSong(AudioTrack track, TextChannel textChannel, String user){
        this.audioTrack = track;
        this.isLoaded = true;
        this.channel = textChannel;
        this.user = user;
    }

   public void setTrackAndSetLoadedTrue(AudioTrack track){
        this.audioTrack = track;
        this.isLoaded = true;
        this.isInLoadingProcess = false;
   }

    public AudioTrack getTrack(){
        return this.audioTrack;
    }


}
