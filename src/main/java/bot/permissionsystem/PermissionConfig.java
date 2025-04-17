package bot.permissionsystem;

import general.SyIO;
import net.dv8tion.jda.api.Permission;

import java.util.*;

public class PermissionConfig {

    public static final Map<BotPermission, Set<Permission>> PERMISSIONS_MAP;
    private static final Permission[] standardPermissions = {
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_HISTORY,
            Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_MENTION_EVERYONE,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_EXT_EMOJI,
            Permission.MESSAGE_EXT_STICKER,
            Permission.MESSAGE_SEND_IN_THREADS
    };

    static {
        PERMISSIONS_MAP = new EnumMap<>(BotPermission.class);
        set(BotPermission.VOICE_NORMAL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
        set(BotPermission.VOICE_ADVANCED, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.VOICE_MOVE_OTHERS);
        set(BotPermission.TEXT_NORMAL, Permission.MESSAGE_SEND);
        set(BotPermission.TEXT_ADVANCED, Permission.MESSAGE_MANAGE);
        set(BotPermission.ADMIN, Permission.ADMINISTRATOR);

        set(BotPermission.BOT_TEXT, standardPermissionsPlus());
        set(BotPermission.BOT_VOICE, standardPermissionsPlus(Permission.VOICE_CONNECT, Permission.VOICE_SPEAK));
        set(BotPermission.BOT_ADMIN, Permission.ADMINISTRATOR);
        for(Map.Entry<BotPermission, Set<Permission>> entry : PERMISSIONS_MAP.entrySet()){
            BotPermission botPermission = entry.getKey();
            if(botPermission != null && botPermission.isBotBotPermission()){
                setDescription(botPermission);
            }
        }
    }

    private static void set(BotPermission botPermission, Permission... permissions) {
        PERMISSIONS_MAP.put(botPermission, EnumSet.copyOf(Arrays.asList(permissions)));
    }

    private static Permission[] standardPermissionsPlus(Permission... permissions) {
        Permission[] result = new Permission[permissions.length + standardPermissions.length];
        System.arraycopy(permissions, 0, result, 0, permissions.length);
        System.arraycopy(standardPermissions, 0, result, permissions.length, standardPermissions.length);
        return result;
    }

    // Unnötig, da die Beschreibung der Berechtigungen für den Bot nicht abgerufen werden.
    private static void setDescription(BotPermission botPermission){
        StringBuilder sb = new StringBuilder();
        for(Permission permission : PERMISSIONS_MAP.get(botPermission)){
            sb.append(permission.getName()).append(", ");
        }
        botPermission.setDescription(SyIO.replaceLast(sb.toString(), ", ", ""));
    }


}
