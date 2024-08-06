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
import java.util.List;
import java.util.regex.Matcher;

public class QueueMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;

        GuildMusicManager musicManager = Bot.instance.getPM().getGuildMusicManager(e.getGuild());

        List<MusicSong> queue = musicManager.scheduler.getQueue();
        if(queue.isEmpty()){
            e.reply("Die Wiedergabeliste ist leer.").setEphemeral(true).queue();
            return true;
        }

        e.deferReply().queue();

        int size = queue.size();

        MusicSong last = musicManager.scheduler.getLastPlaying();
        if(last == null)
            return false;


        EmbedBuilder eb = new EmbedBuilder();
        String uri = last.getTrack().getInfo().uri;
        Matcher matcher = LinkConverter.YOUTUBE_VIDEO_ID_PATTERN.matcher(uri);

        String description;
        try {
            description = "Hinzugefügt von: " + e.getGuild().getMembersByName(last.user, true).get(0).getAsMention();
        } catch (Exception ex){
            description = "Bestimmt ein wunderbarer Song";
        }
        if(musicManager.scheduler.isRepeat()){
            description += "\n**Information:** Dieser Song wird wiederholt.";
        }


        eb.setAuthor("Jetzt spielt");
        eb.setColor(Color.MAGENTA);
        eb.setTitle("**" + last.getTrack().getInfo().title + "**", uri);
        eb.setDescription(description);
        if(matcher.find()){
            eb.setThumbnail("https://img.youtube.com/vi/" + matcher.group(1) + "/0.jpg");
        }

        int length = 0;
        int loaded = 0;

        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(MusicSong song : queue.subList(0, Math.min(10, size))){
            String url;
            if(song.isLoaded){
                String t = song.getTrack().getInfo().title.replaceAll("\\[", "").replaceAll("]", "");
                if (t.length() > 45) t = t.substring(0, 42) + "...";
                length += (int) song.getTrack().getInfo().length;
                url = "[" + t + "](" + song.getTrack().getInfo().uri + ") `[" + calcDuration((int)song.getTrack().getInfo().length) + "]`";
                loaded++;
            }else{
                url = (song.url.startsWith("ytsearch:") ? general.SyIO.replaceLast(song.url.replaceFirst("ytsearch:", ""), " audio", "") : song.url);
            }
            sb.append("`").append(i).append(".` ").append(url).append("\n");
            i++;
        }

        eb.addField("Als nächstes:", sb.toString(), false);

        length += (size - loaded) * 180000;

        eb.addField("Gesamte Warteschlange", "`" + size + " Songs`", true);
        eb.addField("Gesamte Dauer", "`" + calcDuration(length) + "`", true);


        eb.setFooter("Befehl '/queue'");
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
                "Die Wiedergabeliste darf nicht leer sein.";
    }

    @Override
    public String getDescription() {
        return "Erhalte Informationen zur aktuellen Wiedergabeliste";
    }


    public static String calcDuration(int millis){
        int durationInSeconds = millis / 1000;
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;
        int seconds = durationInSeconds % 60;
        String length;
        if (hours > 0) {
            length = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            length = String.format("%02d:%02d", minutes, seconds);
        }
        return length;
    }


}
