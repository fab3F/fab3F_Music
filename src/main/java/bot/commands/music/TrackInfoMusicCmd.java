package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import bot.music.LinkConverter;
import bot.music.MusicSong;
import bot.music.VoiceStates;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;
import java.util.regex.Matcher;

public class TrackInfoMusicCmd implements ServerCommand {

    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        MusicSong last = musicManager.scheduler.getLastPlaying();
        if(last == null)
            return false;


        e.deferReply().queue();


        EmbedBuilder eb = new EmbedBuilder();
        String uri = last.getTrack().getInfo().uri;
        Matcher matcher = LinkConverter.YOUTUBE_VIDEO_ID_PATTERN.matcher(uri);

        String description;
        if(last.user.equalsIgnoreCase(Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0))){
            description = "Dieser Song wurde von YouTube Autoplay ausgewählt.";
        }else{
            try {
                description = "Hinzugefügt von: " + e.getGuild().getMembersByName(last.user, true).get(0).getAsMention();
            } catch (Exception ex){
                description = "Bestimmt ein wunderbarer Song";
            }
        }
        if(musicManager.scheduler.isRepeat()){
            description += "\n**Information:** Dieser Song wird wiederholt.";
        }

        String length = QueueMusicCmd.calcDuration((int) last.getTrack().getInfo().length);

        eb.setAuthor("Jetzt spielt");
        eb.setColor(Color.MAGENTA);
        eb.setTitle("**" + last.getTrack().getInfo().title + "**", uri);
        eb.setDescription(description);
        eb.addField("Uploader", "**`" + last.getTrack().getInfo().author + "`**", true);
        eb.addField("Länge", "**`" + length + "`**", true);
        if(matcher.find()){
            eb.setThumbnail("https://img.youtube.com/vi/" + matcher.group(1) + "/0.jpg");
        }
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
    public BotPermission getUserPermission() {
        return BotPermission.VOICE_NORMAL;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.\n" +
                "Um diesen Befehl auszuführen, muss gerade ein Song abgespielt werden.";
    }

    @Override
    public String getDescription() {
        return "Informationen zu dem Lied, welches gerade abgespielt wird";
    }
}
