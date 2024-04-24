package bot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.List;


public class MusicSong {

    public boolean isLoaded = false;

    public String url;
    public TextChannel channel;
    public User user;
    private final List<AudioTrack> audioTracks;

    public MusicSong(String url, TextChannel textChannel, User user){
        this.url = url;
        this.channel = textChannel;
        this.user = user;
        this.audioTracks = new ArrayList<>();
    }

   public void addTrack(AudioTrack track){
        this.audioTracks.add(track);
        this.isLoaded = true;
   }

    public AudioTrack getNextTrack(){
        return !this.audioTracks.isEmpty() ? audioTracks.remove(0) : null;
    }

    public int amountOfTracks(){
        return this.audioTracks.size();
    }


}
