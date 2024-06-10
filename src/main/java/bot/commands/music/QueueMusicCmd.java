package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.VoiceStates;
import bot.music.MusicSong;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;
import java.util.List;

public class QueueMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(!VoiceStates.inSameVoiceChannel(e.getGuild().getSelfMember(), e.getMember()))
            return false;


        List<MusicSong> queue = Bot.instance.getPM().getGuildMusicManager(e.getGuild()).scheduler.getQueue();
        int remaining = queue.size();
        if(remaining > 10){
            queue = queue.subList(0, 10);
            remaining -= 10;
        }else{
            remaining = -1;
        }

        if(queue.isEmpty()){
            e.reply("Die Wiedergabeliste ist leer.").setEphemeral(true).queue();
            return true;
        }

        e.deferReply().queue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.ORANGE);
        eb.setTitle("**Wiedergabeliste**");
        eb.setDescription("Das sind die nächsten Songs:");
        int i = 1;
        for(MusicSong song : queue){
            String url;
            if(song.isLoaded){
                url = "**" + song.getTrack().getInfo().title + "**";
            }else{
                url = (song.url.startsWith("ytsearch:") ? general.SyIO.replaceLast(song.url.replaceFirst("ytsearch:", ""), " audio", "") : song.url) + " (Noch nicht geladen)";
            }
            eb.addField("#" + i + " " + url, "Hinzugefügt von `" + song.user + "`", false);
            i++;

        }
        if(remaining > 0){
            eb.addField("Anzahl an weiteren Songs in der Liste: **" + remaining + "**", "", false);
        }
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
}
