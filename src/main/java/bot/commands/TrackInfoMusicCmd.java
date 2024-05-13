package bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class TrackInfoMusicCmd implements ServerCommand {

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
        if(!memberVoiceState.getChannel().getId().equals(selfVoiceState.getChannel().getId()))
            return false;



        return false;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public Permission[] getNeededPermissions() {
        return new Permission[]{Permission.VOICE_CONNECT};
    }

    @Override
    public String getUsage() {
        return ServerCommand.super.getUsage() + "\n" +
                """
                    Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                    Um diesen Befehl auszuführen, muss gerade ein Song abgespielt werden.""";
    }
}
