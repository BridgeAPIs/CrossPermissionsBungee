package net.zyuiop.permissions.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.permissions.PermissionUser;

/**
 * This file is licensed under MIT License
 * A copy of the license is provided with the source
 * (C) zyuiop 2015
 */
public class CommandRefresh extends Command {

    private PermissionsAPI api;

    public CommandRefresh(PermissionsAPI api) {
        super("bungeerefresh", null, "brefresh", "prefresh");
        this.api = api;
    }

    protected boolean canDo(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            PermissionUser u = api.getUser(((ProxiedPlayer) sender).getUniqueId());
            if (u.hasPermission("permissions.bungee.*") || u.hasPermission("permissions.*") || u.hasPermission("permissions.bungee.refresh"))
                return true;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!canDo(sender)) {
            sender.sendMessage(ChatColor.RED + "You are not allowed to do that.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Refreshing BungeeCord permissions cache...");
        api.getManager().refresh();
        sender.sendMessage(ChatColor.GREEN + "Cache refreshed !");
    }
}
