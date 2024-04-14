package bot;

import bot.commands.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {

    public ConcurrentHashMap<String, ServerCommand> commands;

    public CommandManager() {

        this.commands = new ConcurrentHashMap<>();

        this.commands.put("ping", new PingCmd());


        this.commands.put("play", new PlayMusicCmd());
        this.commands.put("playnow", new PlayNowMusicCmd());

        this.commands.put("pause", new PauseMusicCmd());
        this.commands.put("continue", new ContinueMusicCmd());

        this.commands.put("queue", new QueueMusicCmd());
        this.commands.put("clearqueue", new ClearQueueMusicCmd());

        this.commands.put("repeat", new RepeatMusicCmd());
        this.commands.put("skip", new SkipMusicCmd());
        this.commands.put("stop", new StopMusicCmd());
        this.commands.put("trackinfo", new TrackInfoMusicCmd());


    }

    public void perform(SlashCommandInteractionEvent e) {

        ServerCommand cmd = this.commands.get(e.getName());

        if(cmd == null) {
            e.reply("Dieser Befehl ist leider nicht verfügbar!").setEphemeral(true).queue();
            return;
        }

        if(e.getMember() == null){
            e.reply("Ein unbekannter Fehler ist aufgetreten.").setEphemeral(true).queue();
            return;
        }

        if (!e.getMember().hasPermission(cmd.getNeededPermission())) {
            e.reply("Du brauchst die Berechtigung " + cmd.getNeededPermission().getName() + ", um diesen Befehl auszuführen!").setEphemeral(true).queue();
            return;
        }

        if (!cmd.peformCommand(e)) {
            try {
                e.reply(cmd.getUsage()).setEphemeral(true).queue();
            } catch (Exception ex) {
                e.reply("Ein unbekannter Fehler ist aufgetreten.").setEphemeral(true).queue();
            }
        }

    }

}