package bot.listener;

import bot.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {

        if(!e.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            stopMusicInGuild(e.getGuild());
        } else {
            if (e.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() < 2) {
                stopMusicInGuild(e.getGuild());
            }
        }

    }

    public void stopMusicInGuild(Guild g) {
        try {
            Bot.instance.getPM().stopGuildMusicManager(g.getId());
        } catch (NullPointerException ignored){}
    }


}
