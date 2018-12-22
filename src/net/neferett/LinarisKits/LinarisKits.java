package net.neferett.LinarisKits;

import net.neferett.LinarisKits.Managers.DatabaseManager;
import net.neferett.LinarisKits.Managers.UsersManager;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LinarisKits
extends JavaPlugin
implements Listener {
    private static LinarisKits instance;

    public static LinarisKits getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.reloadConfig();
        DatabaseManager.getInstance().update();
        DatabaseManager.getInstance().showState();
        this.getCommand("coins").setExecutor(new CommandCoins());
        this.getCommand("addkit").setExecutor(new CommandAddKit());
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        DatabaseManager.getInstance().showState();
        DatabaseManager.closeConnection();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UsersManager.getInstance().removeUserFromCache(event.getPlayer());
    }

    public static int getInt(String key) {
        return instance.getConfig().getInt(key);
    }

    public static boolean getBoolean(String key) {
        return instance.getConfig().getBoolean(key);
    }

    public static String getString(String key) {
        return instance.getConfig().getString(key);
    }
}

