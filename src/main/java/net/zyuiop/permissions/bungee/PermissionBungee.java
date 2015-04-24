package net.zyuiop.permissions.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.database.JedisDatabase;
import net.zyuiop.crosspermissions.api.database.JedisSentinelDatabase;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlayer;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlugin;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This file is licensed under MIT License
 * A copy of the license is provided with the source
 * (C) zyuiop 2015
 */
public class PermissionBungee extends Plugin implements RawPlugin {

    public static PermissionsAPI api;

    public void onEnable() {

        // Registering listener

        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        String defaultGroup = null;

        try {
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                Files.copy(getResourceAsStream("config.yml"), file.toPath());
            }

            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            defaultGroup = config.getString("default-group");

            if (config.getBoolean("redis-sentinel.enabled", false)) {
                logInfo("Trying to load API with database mode : REDIS SENTINEL.");
                String master = config.getString("redis-sentinel.master");
                String auth = config.getString("redis-sentinel.auth");
                List<String> ips = config.getStringList("redis-sentinel.sentinels");

                if (master == null || auth == null || ips == null) {
                    logSevere("Configuration is not complete. Plugin failed to load.");
                    return;
                } else {
                    try {
                        Set<String> iplist = new HashSet<>();
                        iplist.addAll(ips);
                        JedisSentinelDatabase database = new JedisSentinelDatabase(iplist, master, auth);
                        api = new PermissionsAPI(this, config.getString("default-group"), database);
                    } catch (Exception e) {
                        logSevere("Configuration is not correct. Plugin failed to load.");
                        e.printStackTrace();
                        return;
                    }
                }
            } else if (config.getBoolean("redis.enabled", false)) {
                logInfo("Trying to load API with database mode : REDIS.");
                String address = config.getString("redis.address");
                String auth = config.getString("redis.auth");
                int port = config.getInt("redis.port", 6379);

                if (address == null || auth == null) {
                    logSevere("Configuration is not complete. Plugin failed to load.");
                    return;
                } else {
                    try {
                        JedisDatabase database = new JedisDatabase(address, port, auth);
                        api = new PermissionsAPI(this, config.getString("default-group"), database);
                    } catch (Exception e) {
                        logSevere("Configuration is not correct. Plugin failed to load.");
                        e.printStackTrace();
                        return;
                    }
                }
            } else {
                logSevere("ERROR : NO DATABASE BACKEND ENABLED.");
                logSevere("To use this plugin, you have to enable redis or redis sentinel");
                return;
            }
        } catch (Exception e) {
            this.getLogger().info("An error occured while trying to load configuration.");
            this.getLogger().info("API will be loaded with a null default group.");
        }

        this.getProxy().getPluginManager().registerCommand(this, new CommandRefresh(api));
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));

    }

    @Override
    public void logSevere(String s) {
        this.getLogger().severe(s);
    }

    @Override
    public void logWarning(String s) {
        this.getLogger().warning(s);
    }

    @Override
    public void logInfo(String s) {
        this.getLogger().info(s);
    }

    @Override
    public void runRepeatedTaskAsync(Runnable runnable, long delay, long before) {
        this.getProxy().getScheduler().schedule(this, runnable, before * 50, delay * 50, TimeUnit.MILLISECONDS); // Un tick = 50 ms
    }

    @Override
    public void runAsync(Runnable runnable) {
        this.getProxy().getScheduler().runAsync(this, runnable);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return (getProxy().getPlayer(uuid) != null);
    }

    @Override
    public RawPlayer getPlayer(UUID player) {
        return new VirtPlayer(player);
    }

    @Override
    public UUID getPlayerId(String name) {
        ProxiedPlayer player = getProxy().getPlayer(name);
        return (player == null) ? null : player.getUniqueId();
    }

    @Override
    public String getPlayerName(UUID id) {
        ProxiedPlayer player = getProxy().getPlayer(id);
        return (player == null) ? null : player.getName();
    }
}
