package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.permissionsystem.BotPermission;
import general.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ClearQueueMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        try {
            Bot.instance.getPM().getGuildMusicManager(e.getGuild()).clearQueue();
        }catch (Exception ex){
            Main.debug("ERROR when trying to clear queue: " + ex.getMessage());
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
        return BotPermission.VOICE_ADVANCED;
    }

    @Override
    public String getUsage(){
        return """
                Benutze ```/clearqueue```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.""";
    }
}
