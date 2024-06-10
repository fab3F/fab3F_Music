package bot.commands;


import bot.permissionsystem.BotPermission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public interface ServerCommand {

    default String cmdName(){
        return "{cmdName}";
    }

    boolean peformCommand(SlashCommandInteractionEvent event);
    boolean isOnlyForServer();
    BotPermission getUserPermission();

    BotPermission getBotPermission();

    default String getFurtherUsage(){
        return null;
    }

    default String getDescription() {return cmdName() + "-Befehl";}

    default Option[] getOptions(){
        return null;
    }


    class Option{
        public OptionType type;
        public String name;
        public String description;
        public boolean required;
        public Option(OptionType type, String name, String description, boolean required){
            this.type = type;
            this.name = name;
            this.description = description;
            this.required = required;
        }
    }


}

