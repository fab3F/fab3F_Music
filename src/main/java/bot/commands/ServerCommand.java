package bot.commands;


import bot.permissionsystem.BotPermission;

public interface ServerCommand {


    boolean peformCommand(net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent event);
    boolean isOnlyForServer();
    BotPermission getNeededPermission();
    default String getUsage() {
        return "Benutze ```/{cmdName}```";
    };
}
