package bot.commands;

import bot.Bot;
import bot.permissionsystem.BotPermission;
import general.ConfigWorker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ConfigCmd implements ServerCommand{
    @Override
    public String cmdName() {
        return "config";
    }

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent event) {

        event.deferReply().queue();
        List<OptionMapping> options = event.getOptions();
        String id = event.getGuild().getId();

        if(!options.isEmpty()){
            ConfigWorker g = Bot.instance.configWorker;

            for(OptionMapping o : options){
                g.removeAllServerConfig(id, o.getName());
                List<String> v = new ArrayList<>(Arrays.asList(o.getAsString().split(",")));
                if(v.get(0).equals("-1")){
                    v = g.getServerConfig("template", o.getName());
                }
                for(String value : v){
                    g.addServerConfig(id, o.getName(), value);
                }

            }
        }

        event.getHook().sendMessageEmbeds(printCurrent(id)).queue();
        return true;
    }

    private MessageEmbed printCurrent(String guildId){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Konfiguration");
        eb.setDescription("Dies sind die aktuellen Einstellungen des Bots:");
        eb.setColor(Color.CYAN);
        for(Option option : this.getOptions()){
            List<String> values = Bot.instance.configWorker.getServerConfig(guildId, option.name);
            String value = values.size() <= 1 ? values.get(0) : values.toString();
            eb.addField(option.name, value, false);
        }
        eb.setFooter("Befehl '/config'");
        eb.setTimestamp(new Date().toInstant());
        return eb.build();
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.ADMIN;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_TEXT;
    }

    @Override
    public String getFurtherUsage() {
        return """
                Ändere die Konfiguration des Bots wie folgt:
                1) Wähle mindestens eine Option zum Ändern aus.
                2) Gib einen neuen Wert für die ausgewählte Option ein.
                3) Gib den Wert '-1' ein, um den Standard wiederherzustellen.""";
    }

    @Override
    public String getDescription() {
        return "Ändere die Einstellungen des Bots";
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{
                new Option(OptionType.STRING, "defaultautoplaysong", "Standardmäßger Song für Autoplay", false),
                new Option(OptionType.INTEGER, "defaultvolume", "Standardmäßige Lautstärke des Bots (Wert zwischen 0 und 100)", false)
        };
    }
}
