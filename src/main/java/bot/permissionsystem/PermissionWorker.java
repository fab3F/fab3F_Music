package bot.permissionsystem;

import general.Main;
import general.SyIO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.util.Set;

public class PermissionWorker {

    public String hasPermission(Member member, BotPermission botPermission) {
        Set<Permission> requiredPermissions = PermissionConfig.PERMISSIONS_MAP.get(botPermission);
        if (requiredPermissions == null) {
            Main.error("Error 12: requiredPermissions is null for this botPermission: " + botPermission.name());
            return "_FALSE_Error 12: Der erforderlichen Berechtigungen existieren nicht. Bitte versuche es zu einem späteren Zeitpunkt erneut.";
        }

        StringBuilder sb = new StringBuilder();
        for (Permission permission : requiredPermissions.toArray(new Permission[0])) {
            if (!member.hasPermission(permission)) {
                sb.append(permission).append(", ");
            }
        }
        return sb.toString().isEmpty() ? "_TRUE_" : "_FALSE_" + SyIO.replaceLast(sb.toString(), ", ", "");
    }

}
