package bot.permissionsystem;

import net.dv8tion.jda.api.Permission;

import java.util.*;

public class PermissionConfig {

    public static final Map<BotPermission, Set<Permission>> USER_PERMISSIONS_MAP;
    private static final Permission[] standardPermissions = {
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_MENTION_EVERYONE,
            Permission.MESSAGE_EMBED_LINKS
    };

    static {
        USER_PERMISSIONS_MAP = new EnumMap<>(BotPermission.class);
        set(BotPermission.VOICE_NORMAL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
        set(BotPermission.VOICE_ADVANCED, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_MOVE_OTHERS);
        set(BotPermission.TEXT_NORMAL, Permission.MESSAGE_SEND);
        set(BotPermission.TEXT_ADVANCED, Permission.MESSAGE_MANAGE);
        set(BotPermission.ADMIN, Permission.ADMINISTRATOR);

        set(BotPermission.BOT_TEXT, standardPermissionsPlus());
        set(BotPermission.BOT_VOICE, standardPermissionsPlus(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK));
        set(BotPermission.BOT_ADMIN, Permission.ADMINISTRATOR);
    }

    private static void set(BotPermission botPermission, Permission... permissions) {
        USER_PERMISSIONS_MAP.put(botPermission, EnumSet.copyOf(Arrays.asList(permissions)));
    }

    private static Permission[] standardPermissionsPlus(Permission... permissions) {
        Permission[] result = new Permission[permissions.length + standardPermissions.length];
        System.arraycopy(permissions, 0, result, 0, permissions.length);
        System.arraycopy(standardPermissions, 0, result, permissions.length, standardPermissions.length);
        return result;
    }


}
