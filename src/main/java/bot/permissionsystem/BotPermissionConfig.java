package bot.permissionsystem;

import net.dv8tion.jda.api.Permission;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class BotPermissionConfig {

    public static final Map<BotPermission, Set<Permission>> PERMISSIONS_MAP = new EnumMap<>(BotPermission.class);

    static {
        PERMISSIONS_MAP.put(BotPermission.MUSIC_NORMAL, EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK));
        PERMISSIONS_MAP.put(BotPermission.MUSIC_ADVANCED, EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_MOVE_OTHERS));
        PERMISSIONS_MAP.put(BotPermission.TEXT_NORMAL, EnumSet.of(Permission.MESSAGE_SEND));
        PERMISSIONS_MAP.put(BotPermission.TEXT_ADVANCED, EnumSet.of(Permission.MESSAGE_MANAGE));
        PERMISSIONS_MAP.put(BotPermission.ADMIN, EnumSet.of(Permission.ADMINISTRATOR));
    }

}
