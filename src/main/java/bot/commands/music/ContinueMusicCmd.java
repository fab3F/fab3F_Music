package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ContinueMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;


        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        if(!musicManager.audioPlayer.isPaused()){
            e.reply("Die Wiedergabe wird bereits fortgesetzt. Benutze ```/pause``` um die Wiedergabe zu pausieren oder benutze ```/skip``` um einen Song zu überspringen.").queue();
            return true;
        }

        musicManager.audioPlayer.setPaused(false);

        e.reply("Wiedergabe wird fortgesetzt.").queue();

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.VOICE_NORMAL;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_VOICE;
    }

    @Override
    public String getDescription() {
        return "Setzt die Wiedergabe fort.";
    }

    @Override
    public String getFurtherUsage(){
        return """
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Es muss gerade ein Song pausiert sein, um die Wiedergabe fortzusetzen.""";
    }
}
