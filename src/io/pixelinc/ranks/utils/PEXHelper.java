package io.pixelinc.ranks.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PEXHelper {

    /*
     * This is a placeholder in case we want to deny operators from having all
     * permissions.
     */
    public boolean hasPermission(CommandSender sender, String node) {
        if (sender instanceof ConsoleCommandSender)
            return true;

        return sender.hasPermission(node);
    }

    public boolean canManage(CommandSender sender, PermissionEntity target) {
        // Console and operators can manage everyone, for now...
        if (sender instanceof ConsoleCommandSender || sender.isOp())
            return true;

        PermissionUser user = PermissionsEx.getUser((Player) sender);

        // Users can always manage themselves.
        if (target.getIdentifier().equals(((Player) sender).getUniqueId().toString()))
            return true;

        /*
         * Abuses the PEX rank weight system.
         *
         * "Highest" weight is determined by the lowest value. This determines what
         * prefix should be used, what order in a ladder they should be, etc. We're
         * going to be using it to determine how important a rank is for targeting :)
         */
        int userWeight = (user.getOption("weight") == null) ? 100000 : Integer.parseInt(user.getOption("weight"));
        int targetWeight = (target.getOption("weight") == null) ? 100000 : Integer.parseInt(target.getOption("weight"));

        return userWeight < targetWeight;
    }

    // This is stolen from PermissionsEX's PermissionsCommand class
    // And then modified to work for our use case

    public String mapPermissions(String worldName, PermissionEntity entity, int level) {
        StringJoiner builder = new StringJoiner(", ");

        for (String permission : this.getPermissionsTree(entity, worldName, 0)) {
            if (level <= 0)
                builder.add(permission);
        }

        return builder.toString();
    }

    protected List<String> getPermissionsTree(PermissionEntity entity, String world, int level) {
        List<String> permissions = new LinkedList<>();
        Map<String, List<String>> allPermissions = entity.getAllPermissions();

        List<String> worldsPermissions = allPermissions.get(world);
        if (worldsPermissions != null) {
            permissions.addAll(sprintPermissions(world, worldsPermissions));
        }

        for (String parentWorld : PermissionsEx.getPermissionManager().getWorldInheritance(world)) {
            if (parentWorld != null && !parentWorld.isEmpty()) {
                permissions.addAll(getPermissionsTree(entity, parentWorld, level + 1));
            }
        }

        if (level == 0 && world != null && allPermissions.get(null) != null) { // default world permissions
            permissions.addAll(sprintPermissions(null, allPermissions.get(null)));
        }

        return permissions;
    }

    protected List<String> sprintPermissions(String world, List<String> permissions) {
        List<String> permissionList = new LinkedList<>();

        if (permissions == null) {
            return permissionList;
        }

        for (String permission : permissions) {
            permissionList.add(permission + (world != null ? " @" + world : ""));
        }

        return permissionList;
    }

}
