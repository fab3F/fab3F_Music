package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.commands.VoiceStates;
import bot.music.GuildMusicManager;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayMusicCmd implements ServerCommand {
    @Override
    public boolean peformCommand(SlashCommandInteractionEvent e) {
        if(e.getOption("title") == null || e.getMember() == null || e.getGuild() == null){
            return false;
        }
        Guild g = e.getGuild();

        if(!VoiceStates.inVoiceChannel(e.getMember())){
            return false;
        }
        if(VoiceStates.inVoiceChannel(g.getSelfMember())){
            if(!VoiceStates.inSameVoiceChannel(e.getMember(), g.getSelfMember())){
                return false;
            }
        } else {
            final AudioManager audioManager = g.getAudioManager();
            final VoiceChannel memberChannel = e.getMember().getVoiceState().getChannel().asVoiceChannel();
            audioManager.openAudioConnection(memberChannel);
        }


        e.deferReply().queue();

        String link = e.getOption("title").getAsString();

        if(link.contains("\"") || link.contains("'")){
            e.getHook().sendMessage("Sorry, aber diese URL oder dieser Name enthält ein unzulässiges Sonderzeichen. Bitte versuche es erneut.").queue();
            return true;
        }
        GuildMusicManager manager = Bot.instance.getPM().getGuildMusicManager(g);

        if(manager.scheduler.isAutoplay && manager.scheduler.getLastPlaying().user.equals("Premium Autoplayer")){
            manager.clearQueue();
        }

        Bot.instance.getPM().linkConverter.addUrl(link, e, false);

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
        return """
                Benutze ```/play <Name | URL | Playlist>```
                Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.
                Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.""";
    }
}
