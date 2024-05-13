package bot.commands;

import bot.music.GuildMusicManager;
import bot.music.PlayerManager;
import bot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RepeatMusicCmd implements ServerCommand {
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


        GuildMusicManager musicManager = PlayerManager.get.getGuildMusicManager(e.getGuild());


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
    public Permission[] getNeededPermissions() {
        return new Permission[]{Permission.VOICE_CONNECT, Permission.VOICE_SPEAK};
    }

    @Override
    public String getUsage(){
        return """
                Benutze ```/repeat```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Es muss gerade ein Song abgespielt werden.""";
    }
}
