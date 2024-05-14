package bot.commands;

import bot.Bot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import bot.music.*;

public class PlayMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getOption("title") == null){
            return false;
        }

        // User not in voice channel
        if(!e.getMember().getVoiceState().inAudioChannel()){
            return false;
        }

        // not in same voice channel
        if(e.getGuild().getSelfMember().getVoiceState().inAudioChannel()){
            if(!e.getGuild().getSelfMember().getVoiceState().getChannel().getId().equals(e.getMember().getVoiceState().getChannel().getId())){
                return false;
            }

        // Bot not in voice channel
        }else{
            final AudioManager audioManager = e.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) e.getMember().getVoiceState().getChannel();
            audioManager.openAudioConnection(memberChannel);
        }


        e.deferReply().queue();

        String link = e.getOption("title").getAsString();

        if(link.contains("\"") || link.contains("'")){
            e.getHook().sendMessage("Sorry, aber diese URL oder dieser Name enthält ein unzulässiges Sonderzeichen. Bitte versuche es erneut.").queue();
            return true;
        }
        Bot.instance.getPM().linkConverter.addUrl(link, e, false);

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
                Benutze ```/play <Name | URL | Playlist>```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.
                Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.""";
    }
}
