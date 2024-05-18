package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopMusicCmd implements ServerCommand {
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

        try {
            Bot.instance.getPM().stopGuildMusicManager(e.getGuild().getId());
        }catch (Exception ex){
            return false;
        }
        e.reply("Wiedergabe gestoppt und Wiedergabeliste geleert.").queue();
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
                Benutze ```/stop```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.""";
    }
}
