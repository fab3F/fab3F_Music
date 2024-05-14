package bot.commands;

import bot.Bot;
import bot.music.GuildMusicManager;
import bot.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;

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

        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());


        if(musicManager.audioPlayer.getPlayingTrack() == null)
            return false;


        e.deferReply().queue();



        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Informationen zu");
        eb.setColor(Color.MAGENTA);
        eb.setTitle("**`" + musicManager.scheduler.getLastPlaying().getTrack().getInfo().title + "`**");
        eb.setDescription("Bestimmt ein wunderbarer Song");
        eb.addField("Interpret", "**`" + musicManager.scheduler.getLastPlaying().getTrack().getInfo().author + "`**", false);

        int durationInSeconds = (int) musicManager.scheduler.getLastPlaying().getTrack().getInfo().length / 1000;
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;
        String length;
        if (hours > 0) {
            length = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            length = String.format("%02d:%02d", minutes, seconds);
        }

        eb.addField("Länge", "**`" + length + "`**", false);
        eb.addField("URL", "**`" + musicManager.scheduler.getLastPlaying().getTrack().getInfo().uri + "`**", false);
        eb.addField("Hinzugefügt von", "**`" + musicManager.scheduler.getLastPlaying().user.getName() + "`**", false);
        eb.setFooter("Befehl '/trackinfo'");
        eb.setTimestamp(new Date().toInstant());

        e.getHook().sendMessageEmbeds(eb.build()).queue();

        return true;
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
