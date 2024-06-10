package bot.listener;

import bot.Bot;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;


public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
       Bot.instance.getCommandManager().perform(event);
    }

    @Override
    public void onReady(ReadyEvent event) {
        Bot.instance.getCommandManager().updateCommands(event.getJDA());
    }

    @Override
    public void onGuildReady(GuildReadyEvent event){

        event.getGuild().retrieveCommands().queue(guildCommands -> {
            for (Command guildCommand : guildCommands) {
                event.getGuild().deleteCommandById(guildCommand.getId()).queue();
            }
        });

    }

}
