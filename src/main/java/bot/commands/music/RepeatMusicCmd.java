package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RepeatMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;


        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());


        if(musicManager.audioPlayer.getPlayingTrack() == null)
            return false;

        musicManager.scheduler.changeRepeat();

        if(musicManager.scheduler.isRepeat())
            e.reply("Dieser Song wird nun wiederholt.").queue();
        else
            e.reply("Dieser Song wird nun nicht mehr wiederholt.").queue();

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.VOICE_ADVANCED;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.\n" +
                "Es muss gerade ein Song abgespielt werden.";
    }

    @Override
    public String getDescription() {
        return "Startet oder Stoppt das Wiederholen des aktuellen Songs";
    }
}
