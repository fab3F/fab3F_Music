package bot.music;

import bot.Bot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    public static PlayerManager get;
    private final Map<String, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public TrackLoader trackLoader;
    public LinkConverter linkConverter;

    public PlayerManager() {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        this.trackLoader = new TrackLoader(audioPlayerManager);
        this.linkConverter = new LinkConverter(Bot.instance.configWorker.getBotConfig("spotifyClientId").get(0), Bot.instance.configWorker.getBotConfig("spotifyClientId").get(0));
        get = this;
    }


    public GuildMusicManager getGuildMusicManager(Guild guild){
        return this.guildMusicManagers.computeIfAbsent(guild.getId(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void removeGuildMusicManager(String guildId){
        this.guildMusicManagers.remove(guildId);
    }

    public void closeEverything(){
        for(Map.Entry<String, GuildMusicManager> entry : this.guildMusicManagers.entrySet()){
            GuildMusicManager gm = entry.getValue();
            gm.stopEverything();
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

}