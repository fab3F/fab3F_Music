package bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    public static PlayerManager get;
    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public TrackLoader trackLoader;

    private PlayerManager() {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        this.trackLoader = new TrackLoader(audioPlayerManager);
        get = this;
    }



    public GuildMusicManager getGuildMusicManager(Guild guild){
        return this.guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

}