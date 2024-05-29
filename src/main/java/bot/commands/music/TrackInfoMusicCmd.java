package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import bot.music.LinkConverter;
import bot.music.MusicSong;
import bot.music.PlayerManager;
import bot.permissionsystem.BotPermission;
import general.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;
import java.util.regex.Matcher;

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

        MusicSong last = musicManager.scheduler.getLastPlaying();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor("Informationen zu");
        eb.setColor(Color.MAGENTA);
        eb.setTitle("**`" + last.getTrack().getInfo().title + "`**");
        eb.setDescription("Bestimmt ein wunderbarer Song.");
        if(musicManager.scheduler.isRepeat()){
            eb.setDescription("Bestimmt ein wunderbarer Song.\n" +
                    "**Information:** Dieser Song wird wiederholt.");
        }
        eb.addField("Interpret", "**`" + last.getTrack().getInfo().author + "`**", false);

        int durationInSeconds = (int) last.getTrack().getInfo().length / 1000;
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
        String uri = last.getTrack().getInfo().uri;
        eb.addField("URL", uri, false);
        Matcher matcher = LinkConverter.YOUTUBE_VIDEO_ID_PATTERN.matcher(uri);
        if(matcher.find()){
            eb.setThumbnail("https://img.youtube.com/vi/" + matcher.group(1) + "/0.jpg");
        }
        eb.addField("Hinzugefügt von", "**`" + last.user.getName() + "`**", false);
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
    public BotPermission getNeededPermission() {
        return BotPermission.MUSIC_NORMAL;
    }

    @Override
    public String getUsage() {
        return ServerCommand.super.getUsage() + "\n" +
                """
                    Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                    Um diesen Befehl auszuführen, muss gerade ein Song abgespielt werden.""";
    }
}
