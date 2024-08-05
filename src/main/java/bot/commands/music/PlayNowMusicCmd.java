package bot.commands.music;

import bot.Bot;
import bot.commands.ServerCommand;
import bot.music.GuildMusicManager;
import bot.music.VoiceStates;
import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayNowMusicCmd implements ServerCommand {
    @Override
    public String cmdName() {
        return "playnow";
    }

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

        GuildMusicManager manager = Bot.instance.getPM().getGuildMusicManager(g);

        /*


        if(manager.scheduler.isAutoplay && manager.scheduler.getLastPlaying().user.equals(Bot.instance.configWorker.getBotConfig("autoPlayerName").get(0))){
            manager.clearQueue();
        }

        */

        Bot.instance.getPM().linkConverter.addUrl(link, e, true);

        return true;
    }

    @Override
    public boolean isOnlyForServer() {
        return true;
    }

    @Override
    public BotPermission getUserPermission() {
        return BotPermission.VOICE_ADVANCED;
    }

    @Override
    public BotPermission getBotPermission() {
        return BotPermission.BOT_VOICE;
    }

    @Override
    public String getFurtherUsage() {
        return "Um diesen Befehl auszuführen, musst du dich im selben Sprachkanal wie der Bot befinden, falls der Bot bereits in einem Sprachkanal ist.\n" +
                "Es können YouTube-Link, Spotify-Link sowie beliebige Suchbegriffe verwendet werden.";
    }

    @Override
    public String getDescription() {
        return "Spiele einen Song sofort als nächsten ab (Nicht für Playlists verfügbar)";
    }

    @Override
    public Option[] getOptions() {
        return new ServerCommand.Option[]{new Option(OptionType.STRING, "title", "Der Name oder die URL des Songs", true)};
    }
}
