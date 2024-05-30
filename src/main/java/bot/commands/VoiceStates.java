package bot.commands;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

public class VoiceStates {

    public static boolean inSameVoiceChannel(Member self, Member member){
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if(selfVoiceState == null || memberVoiceState == null)
            return false;
        if(!selfVoiceState.inAudioChannel())
            return false;
        if(!memberVoiceState.inAudioChannel())
            return false;
        if(memberVoiceState.getChannel() == null || selfVoiceState.getChannel() == null)
            return false;
        return memberVoiceState.getChannel().getId().equals(selfVoiceState.getChannel().getId());
    }

    public static boolean inVoiceChannel(Member member){
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if(memberVoiceState == null)
            return false;
        return memberVoiceState.inAudioChannel();
    }

}
