package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SkipMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        final Member self = e.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final Member member = e.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();


        if(!selfVoiceState.inAudioChannel())
            return false;
        if(!memberVoiceState.inAudioChannel())
            return false;
        if(!memberVoiceState.getChannel().equals(selfVoiceState.getChannel()))
            return false;


        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());


        if(musicManager.audioPlayer.getPlayingTrack() == null)
            return false;

        musicManager.scheduler.nextSong(true);

        e.reply("Aktueller Song wurde übersprungen.").queue();

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
    public String getUsage(){
        return """
                Benutze ```/skip```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Es muss gerade ein Song abgespielt werden.""";
    }
}
