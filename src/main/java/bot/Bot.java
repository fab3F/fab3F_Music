package bot;

import bot.listener.ChannelListener;
import bot.listener.MessageListener;
import bot.listener.SlashCommandListener;
import bot.music.PlayerManager;
import bot.permissionsystem.PermissionWorker;
import general.ConfigWorker;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

    public static Bot instance;
    public ConfigWorker configWorker;
    public final boolean debug;

    private final CommandManager commandManager;
    private final ShardManager shardManager;

    private PlayerManager playerManager;

    public PermissionWorker pW;

    public Bot(boolean debug, String token, ConfigWorker configWorker){
        instance = this;
        this.commandManager = new CommandManager();
        this.configWorker = configWorker;
        this.debug = debug;

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(getActivity(configWorker.getBotConfig("activity").get(0), configWorker.getBotConfig("activity").get(1)));
        builder.addEventListeners(new SlashCommandListener())
                .addEventListeners(new ChannelListener())
                .addEventListeners(new MessageListener());
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT);
        builder.disableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES);
        builder.disableCache(CacheFlag.ONLINE_STATUS, CacheFlag.ACTIVITY, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS);
        this.shardManager = builder.build();

        this.playerManager = new PlayerManager();
        this.pW = new PermissionWorker();
    }

    public CommandManager getCommandManager(){return this.commandManager;}

    public ShardManager getShardManager(){return this.shardManager;}

    public PlayerManager getPM(){return this.playerManager;}

    public Activity getActivity(String activity, String content){
        return switch (activity) {
            case "playing" -> Activity.playing(content);
            case "listening" -> Activity.listening(content);
            default -> Activity.watching(content);
        };
    }

    public void destroy(){
        this.playerManager.closeEverything();
        this.playerManager = null;

        this.getShardManager().setStatus(OnlineStatus.OFFLINE);
        this.getShardManager().shutdown();

    }
}
