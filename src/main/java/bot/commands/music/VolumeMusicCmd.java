package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class VolumeMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        GuildMusicManager manager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());
        int volume = manager.getVolume();

        if(e.getOption("value") == null){
            e.reply("Die aktuelle Lautstärke beträgt " + volume + "%.").queue();
            return true;
        }

        int newVolume = e.getOption("value").getAsInt();
        newVolume = Math.min(newVolume, 100);
        newVolume = Math.max(newVolume, 0);

        manager.setVolume(newVolume);

        e.reply("Die Lautstärke wurde von " + volume + "% von " + newVolume + "% geändert.").queue();
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
