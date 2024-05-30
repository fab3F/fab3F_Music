package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.commands.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AutoPlayMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;


        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        boolean autoplay = musicManager.scheduler.toogleAutoPlay();
        if(autoplay){
            e.reply("Autoplay wurde aktiviert.").queue();
        }else{
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
        return BotPermission.MUSIC_ADVANCED;
    }

    @Override
    public String getUsage() {
        return """
                Benutze ```/autoplay```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.
                Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.""";
    }
}
