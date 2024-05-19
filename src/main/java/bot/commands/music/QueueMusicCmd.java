package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.MusicSong;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Date;
import java.util.List;

public class QueueMusicCmd implements ServerCommand {
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
                url = (song.url.startsWith("ytsearch:") ? replaceLast(song.url.replaceFirst("ytsearch:", ""), " audio", "") : song.url) + " (Noch nicht geladen)";
            }
            eb.addField("#" + i + " " + url, "Hinzugefügt von `" + song.user.getName() + "`", false);
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
    public Permission[] getNeededPermissions() {
        return new Permission[]{Permission.VOICE_CONNECT, Permission.VOICE_SPEAK};
    }

    @Override
    public String getUsage(){
        return """
                Benutze ```/queue```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden.
                Die Wiedergabeliste darf nicht leer sein.""";
    }

    private String replaceLast(String input, String regex, String replacement) {
        return input.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
}
