package bot;

import bot.commands.*;
import bot.commands.music.*;
import bot.permissionsystem.BotPermission;
import general.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {

    public ConcurrentHashMap<String, ServerCommand> commands;

    public CommandManager() {

        this.commands = new ConcurrentHashMap<>();

        this.commands.put("ping", new PingCmd());
        this.commands.put("help", new HelpCmd());
        this.commands.put("clear", new ClearCmd());

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

        this.commands.put("config", new ConfigCmd());
    }

    public void perform(SlashCommandInteractionEvent e) {
        String cmdName = e.getName();
        ServerCommand cmd = this.commands.get(cmdName);

        if(cmd == null) {
            e.reply("Dieser Befehl ist leider nicht verfügbar!").setEphemeral(true).queue();
            return;
        }

        if(cmd.isOnlyForServer() && (!e.isFromGuild() || e.getGuild() == null)){
            e.reply("Dieser Befehl muss auf einem Server ausgeführt werden.").setEphemeral(true).queue();
            return;
        }

        if(e.getMember() == null){
            e.reply("Ein unbekannter Fehler ist aufgetreten.").setEphemeral(true).queue();
            return;
        }

        if(e.getGuild() != null){
            BotPermission neededBotPerm = cmd.getBotPermission();
            String permCheck1 = Bot.instance.pW.hasPermission(e.getGuild().getSelfMember(), neededBotPerm);
            if(!permCheck1.equals("_TRUE_")){
                e.reply("Dem Bot fehlt die Berechtigungen:\n" + permCheck1.replaceFirst("_FALSE_", "")).setEphemeral(true).queue();
                return;
            }
        }


        BotPermission neededUserPerm = cmd.getUserPermission();
        String permCheck2 = Bot.instance.pW.hasPermission(e.getMember(), neededUserPerm);
        if(!permCheck2.equals("_TRUE_") && !Bot.instance.configWorker.getBotConfig("adminIds").contains(e.getUser().getId())){
            e.reply("Um diesen Befehl auszuführen ist folgende Berechtigungsgruppe erforderlich:\n" +
                    neededUserPerm.name() + " - " + neededUserPerm.getDescription() + "\n" +
                    "Folgende Berechtigung(en) fehlen dir: " + permCheck2.replaceFirst("_FALSE_", "")).setEphemeral(true).queue();
            return;
        }

        //Check ob der Bot in diesem Kanal Nachrichten senden kann (da evtl. privater Kanal)
        if(!e.getChannel().canTalk()){
            e.reply("Der Bot kann in diesem Kanal keine Nachrichten senden, sondern nur auf Befehle wie diesen hier antworten. Dies kann zum Beispiel daran liegen, dass dies ein privater Kanal ist.\n" +
                    "Oft ist es aber erforderlich, dass der Bot noch zusätzliche Nachrichten senden kann. Deshalb muss dieser Befehl in einem anderen Kanal ausgeführt werden oder der Bot braucht Zugriff auf diesen Kanal. " +
                    "Dazu kann man entweder der Rolle des Bots Zugriff auf diesen Kanal geben oder den Bot direkt zu diesem Kanal hinzufügen.").setEphemeral(true).queue();
            return;
        }



        if(cmd.getOptions() != null){
            for(ServerCommand.Option option : cmd.getOptions()){
                if(option.required && e.getOption(option.name) == null) {
                    e.reply(getUsage(cmd, cmdName)).setEphemeral(true).queue();
                    return;
                }
            }
        }


        if (!cmd.peformCommand(e)) {
            try {
                e.reply(getUsage(cmd, cmdName)).setEphemeral(true).queue();
            } catch (Exception ex) {
                Main.error("Bei der Ausführung eines Befehls ist ein unbekannter Fehler aufgetreten: " + e.getCommandString() + "\n" + e.getChannel().getName());
            }
        }

    }

    private String getUsage(ServerCommand cmd, String cmdName){
        StringBuilder usage = new StringBuilder("Benutze `/");
        usage.append(cmd.cmdName().replace("{cmdName}", cmdName));
        if(cmd.getOptions() != null){
            for(ServerCommand.Option option : cmd.getOptions()){
                usage.append(" <").append(option.name).append(">");
            }
        }
        usage.append("`");
        String s;
        if((s = cmd.getFurtherUsage()) != null){
            usage.append("\n").append(s);
        }
        return usage.toString();
    }

    public void updateCommands(JDA jda){

        SlashCommandData[] commandDatas = new SlashCommandData[this.commands.size()];
        int i = 0;
        for (Map.Entry<String, ServerCommand> entry : this.commands.entrySet()) {
            String cmdName = entry.getKey();
            ServerCommand cmd = entry.getValue();
            SlashCommandData slashCommandData = Commands.slash(cmd.cmdName().replace("{cmdName}", cmdName), cmd.getDescription());
            if (cmd.getOptions() != null) {
                for (ServerCommand.Option option : cmd.getOptions()) {
                    slashCommandData = slashCommandData.addOption(option.type, option.name.toLowerCase(Locale.ROOT), option.description.replace("{cmdName}", cmdName), option.required);
                }
            }
            commandDatas[i++] = slashCommandData;
        }

        jda.updateCommands().addCommands(commandDatas).queue();

    }

}