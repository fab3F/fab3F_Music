package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCmd implements ServerCommand{
    @Override
    public boolean isOnlyForServer() {
        return false;
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.getChannel().sendTyping().queue(v -> {
            long ping = System.currentTimeMillis() - time;
            event.reply("Pong! <:table_tennis:944546187724345454> Der Ping des Bots beträgt `" + ping + "ms`!").queue();
        });
        return true;
    }

    @Override
    public Permission[] getNeededPermissions() {
        return new Permission[]{Permission.MESSAGE_SEND};
    }
}
