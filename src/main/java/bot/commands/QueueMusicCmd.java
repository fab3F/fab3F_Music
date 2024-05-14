package bot.commands;

import bot.Bot;
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
        if(queue.isEmpty()){
            e.reply("Die Wiedergabeliste ist leer.").setEphemeral(true).queue();
            return true;
        }

        e.deferReply().queue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.WHITE);
        eb.setTitle("**Wiedergabeliste**");
        eb.setDescription("Das sind die nächsten Songs:");
        for(MusicSong song : queue){
            if(song.isLoaded){
                eb.addField("**" + song.getTrack().getInfo().title + "**", "Hinzugefügt von `" + song.user.getName() + "`", false);
            }else{
                String url = song.url.startsWith("ytsearch:") ? replaceLast(song.url.replaceFirst("ytsearch:", ""), " audio", "") : song.url;
                eb.addField(url + " (Noch nicht geladen)", "Hinzugefügt von `" + song.user.getName() + "`", false);
            }

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
