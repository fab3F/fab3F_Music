package bot.listener;

import bot.Bot;
import general.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(Bot.instance.configWorker.getBotConfig("adminIDs").contains(event.getAuthor().getId())) {
            String msg = event.getMessage().getContentDisplay();
            String[] parts = msg.split(" ");

            if (msg.startsWith("forceshutdown") && parts.length > 1) {
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Shutdown!").queue();
                    Main.main.closeProgram();
                }
            } else if (msg.startsWith("forcerestart") && parts.length > 1) {
                String id = parts[1];
                if (id.equals(event.getGuild().getSelfMember().getId())) {
                    event.getChannel().sendMessage("Restarting!").queue();
                    Main.main.restartBot();
                }
            }
        }
    }

}
