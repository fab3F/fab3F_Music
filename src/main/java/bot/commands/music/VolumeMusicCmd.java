package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import bot.music.VoiceStates;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class VolumeMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {

        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        GuildMusicManager manager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        if(e.getOption("value") == null){
            e.reply("Die aktuelle Lautstärke beträgt " + manager.getVolume() + "%.").queue();
            return true;
        }

        int newVolume = Math.min(Math.max(e.getOption("value").getAsInt(), 0), 100);

        manager.setVolume(newVolume);

        e.reply("Die Lautstärke wurde zu " + newVolume + "% geändert. Max: 100%").queue();
        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.VOICE_NORMAL;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.\n" +
                "Der Wert muss zwischen 0 und 100 liegen.";
    }

    @Override
    public String getDescription() {
        return "Ändere oder erhalte allgemeine Lautstärke des Bots";
    }

    @Override
    public Option[] getOptions() {
        return new Option[]{new Option(OptionType.INTEGER, "value", "Wert zwischen 0 und 100 (Prozent)", false)};
    }
}
