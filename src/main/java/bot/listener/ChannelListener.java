package bot.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent e) {

        if(!e.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            disconnectFromChannel(e.getGuild());
        } else {
            if (e.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() < 2) {
                disconnectFromChannel(e.getGuild());
            }
        }

    }

    public void disconnectFromChannel(Guild g) {

        try {
            GuildMusicManager.stopEverything(PlayerManager.getINSTANCE().musicManagers.get(g.getIdLong()), g);
        } catch (NullPointerException ignored){}

    }


}
