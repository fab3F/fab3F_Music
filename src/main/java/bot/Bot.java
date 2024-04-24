package bot;

import bot.listener.ChannelListener;
import bot.listener.SlashCommandListener;
import general.ConfigWorker;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

    public static Bot instance;
    private final CommandManager commandManager;
    private final ShardManager shardManager;



    public Bot(boolean debug, String token, ConfigWorker configWorker){
        instance = this;
        commandManager = new CommandManager();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(getActivity(configWorker.getBotConfig("activity").get(0), configWorker.getBotConfig("activity").get(1)));
        builder.addEventListeners(new SlashCommandListener())
                        .addEventListeners(new ChannelListener());
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        builder.disableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES);
        builder.disableCache(CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS);
        shardManager = builder.build();


    }

    public CommandManager getCommandManager(){return commandManager;}

    public ShardManager getShardManager(){return shardManager;}

    public Activity getActivity(String activity, String content){
        return switch (activity) {
            case "playing" -> Activity.playing(content);
            case "listening" -> Activity.listening(content);
            default -> Activity.watching(content);
        };
    }

    public void destroy(){

        shardManager.shutdown();

    }

}
