package net.zyuiop.permissions.bungee;

import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

/**
 * This file is licensed under MIT License
 * A copy of the license is provided with the source
 * (C) zyuiop 2015
 */
public class PlayerListener implements Listener {

    private PermissionBungee plugin;

    public PlayerListener(PermissionBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(final PostLoginEvent e) {
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            public void run() {
                plugin.api.getManager().refreshPerms(e.getPlayer().getUniqueId());
                plugin.logInfo("[PostLoginEvent] Applied permissions for player " + e.getPlayer().getUniqueId());
            }
        }, 2, TimeUnit.SECONDS);
        plugin.logInfo("[PostLoginEvent] Registered schedule for " + e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent e) {
        e.registerIntent(plugin);
        plugin.logInfo("[PreLoginEvent] Loading permissions for player " + e.getConnection().getUniqueId());
        plugin.api.getManager().getUser(e.getConnection().getUniqueId()); // On charge les permisisons
        e.completeIntent(plugin);
    }
}
