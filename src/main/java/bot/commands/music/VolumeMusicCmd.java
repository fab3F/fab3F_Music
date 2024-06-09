package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
    public BotPermission getNeededPermission() {
        return BotPermission.VOICE_NORMAL;
    }

    @Override
    public String getUsage() {
        return """
                Benutze ```/volume <value>```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Der Wert muss zwischen 0 und 100 liegen.""";
    }
}
