package io.pixelinc.ranks.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.pixelinc.ranks.Ranks;
import net.md_5.bungee.api.ChatColor;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class RankCommand implements CommandExecutor {

    private String[] help = new String[] { "rank", "<player>/users [set, remove [rank]]" };
    private Ranks plugin;

    public RankCommand(Ranks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rank.use"))
            return false;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("users")) {
                StringBuilder builder = new StringBuilder();
                Collection<PermissionUser> users = PermissionsEx.getPermissionManager().getUsers();

                users.forEach(user -> {
                    builder.append(ChatColor.GRAY + user.getIdentifier());
                    builder.append("\n  ");
                    builder.append(ChatColor.GREEN + user.getName());
                    builder.append(" [");
                    builder.append(String.join(", ", user.getParentIdentifiers()));
                    builder.append("]\n");
                });

                sender.sendMessage(ChatColor.GRAY + "Registered Users: \n " + builder.toString());
                return true;
            }

            /*
             * We allow an offline player so we can edit their ranks, or preset ranks for
             * users who may not have joined. The disadvantage of this is they can't use
             * partial names, ie. pixel would auto set to PixeLInc inside the getPlayer()
             * due to offline players.
             */
            String targetName = args[0];
            PermissionUser permissionUser = PermissionsEx.getUser(targetName);

            /*
             * TODO: Implement a better system for this, not like we need a full fledged
             * (command) system for this, though.
             */

            if (args.length == 1) {
                StringBuilder builder = new StringBuilder();

                builder.append(ChatColor.GRAY + "User Info of ");
                builder.append(ChatColor.AQUA + permissionUser.getName());
                builder.append("\n");

                /*
                 * If we can find a valid player, with a valid uuid, print the message if
                 * necessary, otherwise, yeet out.
                 */
                if (permissionUser.getIdentifier().split("-").length == 5) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(permissionUser.getIdentifier()));
                    if (player != null && player.isOp())
                        builder.append(ChatColor.RED + "User is an operator\n");
                }

                builder.append(ChatColor.GRAY + "Groups: ");
                builder.append(ChatColor.GREEN + String.join(", ", permissionUser.getParentIdentifiers("world")));
                builder.append("\n");

                builder.append(ChatColor.GRAY + "Permissions: ");
                builder.append(ChatColor.GREEN + plugin.pexHelper.mapPermissions("world", permissionUser, 0));
                builder.append("\n");

                builder.append(ChatColor.GRAY + "Prefix: ");
                builder.append(ChatColor.translateAlternateColorCodes('&',
                        (permissionUser.getOwnPrefix() == null) ? "" : permissionUser.getOwnPrefix()));
                builder.append("\n");

                sender.sendMessage(builder.toString());
                return true;
            }

            if (args[1].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("rank.users.set")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to set user's ranks!");
                    return false;
                }

                if (args.length < 3) {
                    sender.sendMessage(
                            ChatColor.RED + "Please specify a rank to set, use /group list to see the ranks!");
                    return false;
                }
                String rank = args[2];

                if (!plugin.pexHelper.canManage(sender, permissionUser)) {
                    sender.sendMessage(ChatColor.RED + "You cannot set the rank of this user!");
                    return false;
                }
                PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(rank);

                if (!plugin.pexHelper.canManage(sender, group)) {
                    sender.sendMessage(ChatColor.RED + "You can't set the rank of a group you can't manage!");
                    return false;
                }

                permissionUser.setParentsIdentifier(Arrays.asList(rank));
                sender.sendMessage(ChatColor.GREEN + "Set " + permissionUser.getName() + " to " + rank);
                return true;
            }

            if (args[1].equalsIgnoreCase("remove")) {
                if (!sender.hasPermission("rank.users.remove")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to remove user's ranks!");
                    return false;
                }

                if (!plugin.pexHelper.canManage(sender, permissionUser)) {
                    sender.sendMessage(ChatColor.RED + "You cannot remove the rank of this user!");
                    return false;
                }

                permissionUser.setParentsIdentifier(Arrays.asList("default"));
                sender.sendMessage(ChatColor.GREEN + "Removed all groups from " + permissionUser.getName());
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Invalid arguments, supported arguments: " + help[1]);

        return false;
    }

}
