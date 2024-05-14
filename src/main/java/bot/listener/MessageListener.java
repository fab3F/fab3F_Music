package bot.listener;

import bot.Bot;
import general.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(Bot.instance.configWorker.getBotConfig("ownerId").get(0)) && event.getMessage().getContentDisplay().startsWith("forceshutdown")){
            String id = event.getMessage().getContentDisplay().split(" ")[1];
            if(id.equals(event.getGuild().getSelfMember().getId())){
                event.getChannel().sendMessage("Shutdown!").queue();
                Main.main.closeProgram();
            }
        }

        if(event.getAuthor().getId().equals(Bot.instance.configWorker.getBotConfig("ownerId").get(0)) && event.getMessage().getContentDisplay().startsWith("forcerestart")){
            String id = event.getMessage().getContentDisplay().split(" ")[1];
            if(id.equals(event.getGuild().getSelfMember().getId())){
                event.getChannel().sendMessage("Restarting!").queue();
                Main.main.restartBot();
            }
        }

    }

}
