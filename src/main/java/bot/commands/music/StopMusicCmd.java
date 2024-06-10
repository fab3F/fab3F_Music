package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        try {
            Bot.instance.getPM().stopGuildMusicManager(e.getGuild().getId());
        }catch (Exception ex){
            return false;
        }
        e.reply("Wiedergabe gestoppt und Wiedergabeliste geleert.").queue();
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
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.";
    }

    @Override
    public String getDescription() {
        return "Stoppt die Musik und löscht die Wiedergabeliste";
    }
}
