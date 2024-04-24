package bot.commands;


public interface ServerCommand {


    boolean peformCommand(net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent event);
    boolean isOnlyForServer();
    net.dv8tion.jda.api.Permission[] getNeededPermissions();
    default String getUsage() {
        return "Benutze ```/{cmdName}```";
    };
}
