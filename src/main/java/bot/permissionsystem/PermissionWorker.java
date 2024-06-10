package bot.permissionsystem;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Set;

public class PermissionWorker {

    public boolean hasPermission(Member member, BotPermission botPermission) {
        Set<Permission> requiredPermissions = PermissionConfig.PERMISSIONS_MAP.get(botPermission);
        if (requiredPermissions == null) {
            return false;
        }
        return hasDiscordPermissions(member, requiredPermissions.toArray(new Permission[0]));
    }

    private boolean hasDiscordPermissions(Member member, Permission... permissions) {
        for (Permission permission : permissions) {
            if (!member.hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

}
