package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private final Map<String, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public TrackLoader trackLoader;
    public LinkConverter linkConverter;

    public PlayerManager() {

        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager(true));
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        this.trackLoader = new TrackLoader(audioPlayerManager);
        this.linkConverter = new LinkConverter(Bot.instance.configWorker.getBotConfig("spotifyClientId").get(0), Bot.instance.configWorker.getBotConfig("spotifyClientSecret").get(0));
    }


    public GuildMusicManager getGuildMusicManager(Guild guild){
        GuildMusicManager gm = this.guildMusicManagers.get(guild.getId());
        if (gm == null) {
            gm = new GuildMusicManager(this.audioPlayerManager, guild);
            this.guildMusicManagers.put(guild.getId(), gm);
        }
        return gm;
    }

    public void stopGuildMusicManager(String guildId){
        GuildMusicManager gm = this.guildMusicManagers.remove(guildId);
        if(gm != null){
            gm.stopManager();
        }
    }

    public void closeEverything(){
        for(Map.Entry<String, GuildMusicManager> entry : this.guildMusicManagers.entrySet()){
            GuildMusicManager gm = this.guildMusicManagers.remove(entry.getKey());
            if(gm != null){
                gm.stopManager();
            }
        }

        this.guildMusicManagers.clear();

        this.linkConverter.close();
        this.linkConverter = null;

        this.trackLoader.stopThread();
        this.trackLoader = null;

        this.audioPlayerManager.shutdown();
        this.audioPlayerManager = null;

        // this = null;

    }

    public AudioPlayerManager getAudioPlayerManager(){return this.audioPlayerManager;}

}