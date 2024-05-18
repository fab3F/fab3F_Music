package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ContinueMusicCmd implements ServerCommand {
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

        if(!musicManager.audioPlayer.isPaused()){
            e.reply("Die Wiedergabe wird bereits fortgesetzt. Benutze ```/pause``` um die Wiedergabe zu pausieren oder benutze ```/skip``` um einen Song zu überspringen.").queue();
            return true;
        }

        musicManager.audioPlayer.setPaused(false);

        e.reply("Wiedergabe wird fortgesetzt.").queue();

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
                Benutze ```/continue```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Es muss gerade ein Song pausiert sein, um die Wiedergabe fortzusetzen.""";
    }
}
