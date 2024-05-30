package bot.commands;

import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCmd implements ServerCommand{
    @Override
    public boolean isOnlyForServer() {
        return false;
    }

    @Override
    public BotPermission getNeededPermission() {
        return BotPermission.TEXT_NORMAL;
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
}
