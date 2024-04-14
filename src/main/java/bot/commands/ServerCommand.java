package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


public interface ServerCommand {

    boolean peformCommand(SlashCommandInteractionEvent event);
    Permission getNeededPermission();
    String getUsage();
}
