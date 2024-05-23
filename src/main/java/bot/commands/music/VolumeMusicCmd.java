package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class VolumeMusicCmd implements ServerCommand {
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

        GuildMusicManager manager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        if(e.getOption("value") == null){
            int volume = manager.getVolume();
            e.reply("Die aktuelle Lautstärke beträgt " + volume + " von 100.").queue();
            return true;
        }

        int newVolume = e.getOption("value").getAsInt();
        newVolume = Math.min(newVolume, 100);
        newVolume = Math.max(newVolume, 0);

        manager.setVolume(newVolume);

        e.reply("Die Lautstärke wurde zu " + newVolume + " von 100 geändert.").queue();
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
    public String getUsage() {
        return """
                Benutze ```/volume <value>```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Der Wert muss zwischen 0 und 100 liegen.""";
    }
}
