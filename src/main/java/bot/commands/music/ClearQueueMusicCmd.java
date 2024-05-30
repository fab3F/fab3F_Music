package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.commands.VoiceStates;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ClearQueueMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        try {
            Bot.instance.getPM().getGuildMusicManager(e.getGuild()).clearQueue();
        }catch (Exception ex){
            return false;
        }
        e.reply("Wiedergabeliste erfolgreich geleert.").queue();
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
    public String getUsage(){
        return """
                Benutze ```/clearqueue```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.""";
    }
}
