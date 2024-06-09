package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class AutoPlayMusicCmd implements ServerCommand {
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
        musicManager.scheduler.lastUsedTextChannel = e.getChannel().asTextChannel();
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
    public BotPermission getNeededPermission() {
        return BotPermission.VOICE_ADVANCED;
    }

    @Override
    public String getUsage() {
        return """
                Benutze ```/autoplay```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.
                Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.""";
    }
}
