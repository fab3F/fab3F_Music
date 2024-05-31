package bot;

import bot.commands.HelpCommand;
import bot.commands.PingCmd;
import bot.commands.ServerCommand;
import bot.commands.music.*;
import bot.permissionsystem.BotPermission;
import general.Main;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {

    public ConcurrentHashMap<String, ServerCommand> commands;

    public CommandManager() {

        this.commands = new ConcurrentHashMap<>();

        this.commands.put("ping", new PingCmd());
        this.commands.put("help", new HelpCommand());

        this.commands.put("play", new PlayMusicCmd());
        this.commands.put("playnow", new PlayNowMusicCmd());

        this.commands.put("pause", new PauseMusicCmd());
        this.commands.put("continue", new ContinueMusicCmd());
        this.commands.put("resume", new ContinueMusicCmd());

        this.commands.put("queue", new QueueMusicCmd());
        this.commands.put("clearqueue", new ClearQueueMusicCmd());

        this.commands.put("repeat", new RepeatMusicCmd());
        this.commands.put("skip", new SkipMusicCmd());
        this.commands.put("stop", new StopMusicCmd());
        this.commands.put("leave", new StopMusicCmd());
        this.commands.put("trackinfo", new TrackInfoMusicCmd());
        this.commands.put("volume", new VolumeMusicCmd());
        this.commands.put("bassboost", new BassBoostMusicCmd());
        this.commands.put("autoplay", new AutoPlayMusicCmd());

    }

    public void perform(SlashCommandInteractionEvent e) {
        String cmdName = e.getName();
        ServerCommand cmd = this.commands.get(cmdName);

        if(cmd == null) {
            e.reply("Dieser Befehl ist leider nicht verfügbar!").setEphemeral(true).queue();
            return;
        }

        if(e.getMember() == null){
            e.reply("Ein unbekannter Fehler ist aufgetreten.").setEphemeral(true).queue();
            return;
        }

        if(cmd.isOnlyForServer() && !e.isFromGuild()){
            e.reply("Dieser Befehl muss auf einem Server ausgeführt werden.").setEphemeral(true).queue();
            return;
        }

        BotPermission neededPerm = cmd.getNeededPermission();
        if(!Bot.instance.pW.hasBotPermission(e.getMember(), neededPerm)){
            e.reply("Dir fehlt folgende Berechtigung, um diesen Befehl auszuführen: " + neededPerm.name() + " - " + neededPerm.getDescription()).setEphemeral(true).queue();
            return;
        }


        if (!cmd.peformCommand(e)) {
            try {
                e.reply(cmd.getUsage().replace("{cmdName}", cmdName)).setEphemeral(true).queue();
            } catch (Exception ex) {
                Main.error("Bei der Ausführung eines Befehls ist ein unbekannter Fehler aufgetreten: " + e.getCommandString() + "\n" + e.getChannel().getName());
            }
        }

    }

}