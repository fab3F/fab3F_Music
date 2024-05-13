package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class QueueMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {
        return false;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public Permission[] getNeededPermissions() {
        return new Permission[]{Permission.VOICE_CONNECT, Permission.VOICE_SPEAK};
    }

    @Override
    public String getUsage(){
        return """
                Benutze ```/queue```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.""";
    }
}
