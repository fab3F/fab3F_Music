package bot.permissionsystem;

import net.dv8tion.jda.api.Permission;

import java.util.*;

public class BotPermissionConfig {

    public static final Map<BotPermission, Set<Permission>> PERMISSIONS_MAP;

    static {
        PERMISSIONS_MAP = new EnumMap<>(BotPermission.class);
        set(BotPermission.MUSIC_NORMAL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
        set(BotPermission.MUSIC_ADVANCED, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_MOVE_OTHERS);
        set(BotPermission.TEXT_NORMAL, Permission.MESSAGE_SEND);
        set(BotPermission.TEXT_ADVANCED, Permission.MESSAGE_MANAGE);
        set(BotPermission.ADMIN, Permission.ADMINISTRATOR);
    }

    private static void set(BotPermission botPermission, Permission... permissions) {
        PERMISSIONS_MAP.put(botPermission, EnumSet.copyOf(Arrays.asList(permissions)));
    }

}
