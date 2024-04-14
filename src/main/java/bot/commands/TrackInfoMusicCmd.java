package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TrackInfoMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {
        return false;
    }

    @Override
    public Permission getNeededPermission() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
    }
}
