package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.commands.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class BassBoostMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getOption("value") == null){
            return false;
        }

        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        GuildMusicManager manager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        int boost = e.getOption("value").getAsInt();
        boost = Math.min(boost, 200);
        boost = Math.max(boost, 0);

        manager.setBassBost((float) boost);

        e.reply("Der Bass Boost wurde zu " + boost + "% geändert.").queue();
        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getNeededPermission() {
        return BotPermission.MUSIC_ADVANCED;
    }

    @Override
    public String getUsage() {
        return """
                Benutze ```/bassboost <value>```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Der Wert muss zwischen 0 und 200 liegen.""";
    }
}
