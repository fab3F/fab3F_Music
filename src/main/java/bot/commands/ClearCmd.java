package bot.commands;

import bot.permissionsystem.BotPermission;
import general.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClearCmd implements ServerCommand{
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {
        if(event.getOption("amount") == null){
            return false;
        }

        int amount = event.getOption("amount").getAsInt();

        if(amount<2 || amount > 100){
            return false;
        }

        List<Message> msgList = event.getChannel().getHistory().retrievePast(amount).complete();
        try{
            event.getChannel().asTextChannel().deleteMessages(msgList).queue();
        }catch(IllegalArgumentException ex){
            event.reply("Fehler beim Löschen: Die Nachrichten dürfen nicht älter als zwei Wochen sein.").queue();
            return true;
        }

        event.reply(amount + " Nachrichten wurden gelöscht (Diese Nachricht löscht sich selbst)").queue();

        try {
            event.getHook().deleteOriginal().queueAfter(5, TimeUnit.SECONDS);
        } catch (Exception ex){
            Main.debug("Exception when trying to delete ClearCmd Message: " + ex.getMessage());
        }

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.TEXT_ADVANCED;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_ADMIN;
    }

    @Override
    public String getFurtherUsage() {
        return "Die Anzahl der zu löschenden Nachrichten muss größer als 1 sein und darf nicht größer als 100 sein!";
    }

    @Override
    public String getDescription() {
        return "Lösche eine bestimmte Menger der letzten Nachrichten im Channel";
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{new Option(OptionType.INTEGER, "amount", "Die Anzahl der Nachrichten, die gelöscht werden soll", true)};
    }
}
