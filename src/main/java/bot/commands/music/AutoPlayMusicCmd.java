package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import bot.music.MusicSong;
import bot.music.VoiceStates;
import bot.permissionsystem.BotPermission;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class AutoPlayMusicCmd implements ServerCommand {
    @Override
    public String cmdName() {
        return "autoplay";
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getMember() == null || e.getGuild() == null){
            return false;
        }
        Guild g = e.getGuild();
        if(!VoiceStates.inVoiceChannel(e.getMember())){
            return false;
        }
        if(VoiceStates.inVoiceChannel(g.getSelfMember())){
            if(!VoiceStates.inSameVoiceChannel(e.getMember(), g.getSelfMember())){
                return false;
            }
        } else {
            final AudioManager audioManager = g.getAudioManager();
            final VoiceChannel memberChannel = e.getMember().getVoiceState().getChannel().asVoiceChannel();
            audioManager.openAudioConnection(memberChannel);
        }


        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());
        if(musicManager.scheduler.getLastPlaying() == null){
            musicManager.scheduler.getLastPlaying(placeholerSong(e.getChannel().asTextChannel(), e.getUser().getName()));
        }
        boolean autoplay = musicManager.scheduler.toogleAutoPlay();

        if(autoplay){
            e.reply("Autoplay wurde aktiviert. Nachdem die Wiedergabeliste abgespielt wurde, werden empfohlene Songs abgespielt.").queue();
        }else{
            if(musicManager.scheduler.getLastPlaying().user.equals(Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0))){
                musicManager.clearQueue();
            }
            e.reply("Autoplay wurde deaktiviert.").queue();
        }
        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.VOICE_ADVANCED;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.\n" +
                "Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.";
    }

    @Override
    public String getDescription() {
        return "Spiele automatisch weitere Songs ab, sobald die Wiedergabeliste leer ist oder höre aktuelle Charts!";
    }

    private static MusicSong placeholerSong(TextChannel channel, String username){
        return new MusicSong("STARTNEWAUTOPLAY", channel, username);
    }
}
