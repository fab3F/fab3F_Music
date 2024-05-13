package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {
        event.reply("Alle wichtigen Informationen findest du auf dieser Seite: https://fab3F.github.io/projects/discordbot/help\n" +
                "Falls du trotzdem weitere Hilfe benötigst oder Fragen hast, wende dich bitte an den Support des Servers.").setEphemeral(true).queue();
        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return false;
    }

    @Override
    public Permission[] getNeededPermissions() {
        return new Permission[]{Permission.MESSAGE_SEND};
    }

    @Override
    public String getUsage() {
        return """
                Benutze ```/help```
                Dies kann dir bei Problemen weiterhelfen.""";
    }
}