package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayNowMusicCmd implements ServerCommand {
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
        Bot.instance.getPM().linkConverter.addUrl(link, e, true);

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getNeededPermission() {
        return BotPermission.MUSIC_ADVANCED;
    }

    @Override
    public String getUsage() {
        return """
                Benutze ```/playnow <Name | URL | Playlist>```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.
                Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.""";
    }
}
