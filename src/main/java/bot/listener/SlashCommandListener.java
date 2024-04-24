package bot.listener;

import bot.Bot;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;


public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
       Bot.instance.getCommandManager().perform(event);
    }

    @Override
    public void onReady(ReadyEvent event){

        event.getJDA().updateCommands().addCommands(


                Commands.slash("ping", "Berechne den Ping des Bots"),

                Commands.slash("play", "Spiele einen Song oder eine Playlist ab")
                        .addOption(OptionType.STRING, "title", "Der Name oder URL des Songs oder eine Spotify/YouTube Playlist", true),
                Commands.slash("playnow", "Spiele einen Song oder eine Playlist sofort als nächstes ab")
                        .addOption(OptionType.STRING, "title", "Der Name oder URL des Songs oder eine Spotify/YouTube Playlist", true),

                Commands.slash("pause", "Pausiert die Wiedergabe des aktuellen Songs"),
                Commands.slash("continue", "Setzt die Wiedergabe fort"),

                Commands.slash("queue", "Erhalte Informationen zur aktuellen Wiedergabeliste"),
                Commands.slash("clearqueue", "Leert die Wiedergabeliste"),

                Commands.slash("repeat", "Startet oder Stoppt das Wiederholen des aktuellen Songs"),
                Commands.slash("skip", "Überspringt den aktuellen Song"),
                Commands.slash("stop", "Stoppt die Musik und löscht die Wiedergabeliste"),
                Commands.slash("trackinfo", "Informationen zu dem Lied, was gerade abgespielt wird")



        ).queue();

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