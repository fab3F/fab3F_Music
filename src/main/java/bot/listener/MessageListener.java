package bot.listener;

import bot.Bot;
import general.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(Bot.instance.configWorker.getBotConfig("ownerId").get(0)) && event.getMessage().getContentDisplay().equals("forceshutdown")){
            event.getChannel().sendMessage("Shutdown!").queue();
            Main.main.closeProgram();
        }

    }

}
