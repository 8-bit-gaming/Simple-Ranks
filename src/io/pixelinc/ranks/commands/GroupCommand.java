package io.pixelinc.ranks.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.pixelinc.ranks.Ranks;
import net.md_5.bungee.api.ChatColor;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class GroupCommand implements CommandExecutor {

    private String[] help = new String[] { "group",
            "list/<group_name> [add <perm(s)>, remove <perm(s)>, create, delete, prefix [prefix], suffix [suffix]]" };
    private Ranks plugin;

    public GroupCommand(Ranks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("group.use"))
            return false;

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                List<PermissionGroup> groups = PermissionsEx.getPermissionManager().getGroupList();
                StringBuilder builder = new StringBuilder();

                groups.forEach(group -> {
                    if (plugin.pexHelper.canManage(sender, group))
                        builder.append(ChatColor.GREEN);
                    else
                        builder.append(ChatColor.RED);

                    builder.append(group.getName());
                    builder.append("\n");
                });

                sender.sendMessage(ChatColor.GRAY + "Groups: \n" + builder.toString());
                return true;
            }

            String groupName = args[0];
            PermissionGroup group = PermissionsEx.getPermissionManager().getGroup(groupName);

            // If they just inputed the group name
            if (args.length == 1) {
                StringBuilder builder = new StringBuilder();

                builder.append(ChatColor.GRAY + "Info for ");
                builder.append(ChatColor.GREEN + group.getName());
                builder.append("\n");

                builder.append(ChatColor.GRAY + "Permissions: ");
                builder.append(ChatColor.GREEN + plugin.pexHelper.mapPermissions("", group, 0));
                builder.append("\n");

                builder.append(ChatColor.GRAY + "Inherits from: ");
                builder.append(ChatColor.GREEN + String.join(", ", group.getParentIdentifiers()));
                builder.append("\n");

                builder.append(ChatColor.GRAY + "Members: ");
                builder.append(ChatColor.AQUA + String.join(", ",
                        group.getActiveUsers().stream().map(user -> user.getName()).collect(Collectors.toList())));
                builder.append("\n");

                builder.append(ChatColor.GRAY + "Options: ");
                builder.append("\n");
                builder.append(ChatColor.GRAY + " Default: ");
                builder.append(
                        (group.getOption("default") != null) ? ChatColor.GREEN + "True" : ChatColor.RED + "False");
                builder.append("\n");

                builder.append(ChatColor.GRAY + " Prefix: " + ChatColor.RESET);
                builder.append(ChatColor.translateAlternateColorCodes('&', group.getPrefix()));
                builder.append("\n");

                builder.append(ChatColor.GRAY + " Suffix: " + ChatColor.RESET);
                builder.append(ChatColor.translateAlternateColorCodes('&', group.getSuffix()));
                builder.append("\n");

                sender.sendMessage(builder.toString());
                return true;
            }

            if (args[1].equalsIgnoreCase("create")) {
                if (!sender.hasPermission("group.create")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to create groups!");
                    return false;
                }

                if (!group.isVirtual()) {
                    sender.sendMessage(ChatColor.RED + args[1] + " already exists!");
                    return false;
                }

                sender.sendMessage(ChatColor.GREEN + "Created " + group.getName());
                group.save();
                return true;
            }

            if (args[1].equalsIgnoreCase("delete")) {
                if (!sender.hasPermission("group.create")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to delete groups!");
                    return false;
                }

                sender.sendMessage(ChatColor.GREEN + "Deleted " + group.getName());
                group.remove();
                PermissionsEx.getPermissionManager().resetGroup(group.getIdentifier());
                return true;
            }

            if (args[1].equalsIgnoreCase("prefix")) {
                if (!sender.hasPermission("group.manage")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to manage groups!");
                    return false;
                }

                if (args.length < 3) {
                    sender.sendMessage(
                            ChatColor.GRAY + "The prefix of " + group.getName() + " is " + group.getPrefix());
                    return true;
                }

                String newPrefix = String.join(" ", Arrays.asList(args).stream().skip(2).collect(Collectors.toList()));
                sender.sendMessage(ChatColor.GRAY + " Set prefix of " + group.getName() + " to " + newPrefix);
                group.setPrefix(newPrefix, null);
                return true;
            }

            if (args[1].equalsIgnoreCase("suffix")) {
                if (!sender.hasPermission("group.manage")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to manage groups!");
                    return false;
                }

                if (args.length < 3) {
                    sender.sendMessage(
                            ChatColor.GRAY + "The suffix of " + group.getName() + " is " + group.getSuffix());
                    return true;
                }

                String newSuffix = String.join(" ", Arrays.asList(args).stream().skip(2).collect(Collectors.toList()));
                sender.sendMessage(ChatColor.GRAY + " Set suffix of " + group.getName() + " to " + newSuffix);
                group.setSuffix(newSuffix, null);
                return true;
            }

            if (args.length >= 3) {
                List<String> permissionNodes = Arrays.asList(args).stream().skip(2).collect(Collectors.toList());

                if (args[1].equalsIgnoreCase("add")) {
                    if (!sender.hasPermission("group.manage")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to manage groups!");
                        return false;
                    }

                    sender.sendMessage(ChatColor.GRAY + "Added " + ChatColor.GREEN + String.join(", ", permissionNodes)
                            + ChatColor.GRAY + " to " + ChatColor.AQUA + group.getName());
                    permissionNodes.forEach(group::addPermission);
                    return true;
                }

                if (args[1].equalsIgnoreCase("remove")) {
                    if (!sender.hasPermission("group.manage")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to manage groups!");
                        return false;
                    }

                    sender.sendMessage(
                            ChatColor.GRAY + "Removed " + ChatColor.GREEN + String.join(", ", permissionNodes)
                                    + ChatColor.GRAY + " from " + ChatColor.AQUA + group.getName());
                    permissionNodes.forEach(group::removePermission);
                    return true;
                }
            }

        }

        sender.sendMessage(ChatColor.RED + "Invalid arguments, supported arguments: " + help[1]);

        return false;
    }

}
