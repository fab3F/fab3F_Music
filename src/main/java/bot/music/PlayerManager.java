package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PlayerManager {

    private final Map<String, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public TrackLoader trackLoader;
    public LinkConverter linkConverter;

    public PlayerManager() {
        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager(true);
        //source.useOauth2(new Oauth2Handler().getRefreshToken(), true);
        audioPlayerManager.registerSourceManager(source);
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
        Iterator<Map.Entry<String, GuildMusicManager>> iterator = this.guildMusicManagers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, GuildMusicManager> entry = iterator.next();
            GuildMusicManager gm = entry.getValue();
            if (gm != null) {
                gm.stopManager();
            }
            iterator.remove();
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