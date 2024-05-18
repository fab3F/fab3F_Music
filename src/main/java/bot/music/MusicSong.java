package bot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;


public class MusicSong {

    public boolean isLoaded = false;
    public boolean invalid = false;
    public boolean isInLoadingProcess = false;


    public String url;
    public TextChannel channel;
    public User user;
    private AudioTrack audioTrack;

    public MusicSong(String url, TextChannel textChannel, User user){
        this.url = url;
        this.channel = textChannel;
        this.user = user;
        this.audioTrack = null;
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
