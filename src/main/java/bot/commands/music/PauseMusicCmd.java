package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PauseMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;


        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());


        if(musicManager.audioPlayer.getPlayingTrack() == null)
            return false;

        if(musicManager.audioPlayer.isPaused()){
            e.reply("Die Wiedergabe ist bereits pausiert. Benutze ```/continue``` um die Wiedergabe fortzusetzen.").queue();
            return true;
        }

        musicManager.audioPlayer.setPaused(true);

        e.reply("Wiedergabe pausiert.").queue();

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getNeededPermission() {
        return BotPermission.VOICE_NORMAL;
    }

    @Override
    public String getUsage(){
        return """
                Benutze ```/pause```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Es muss gerade ein Song abgespielt werden.""";
    }
}
