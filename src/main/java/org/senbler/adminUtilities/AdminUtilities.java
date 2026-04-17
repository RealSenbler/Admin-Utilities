package org.senbler.adminUtilities;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.senbler.adminUtilities.commands.*;
import org.senbler.adminUtilities.listeners.*;
import org.senbler.adminUtilities.menus.Menus;

public final class AdminUtilities extends JavaPlugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        Menus.initialize();

        getCommand("adminutilities").setExecutor(new AdminUtilitiesCommand());
        getCommand("freeze").setExecutor(new FreezeCommand());
        getCommand("unfreeze").setExecutor(new UnfreezeCommand());

        //getServer().getPluginManager().registerEvents(new testMenu(Menus.whitelistMenu), this);
        getServer().getPluginManager().registerEvents(new Menus(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);

        getLogger().info("[AdminUtilities] Enabled");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("[AdminUtilities] Disabled");
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static void sendMessage(Player player, String message) {
        final String prefix = "§7[§cAdmin Utilities§7]";
        message = prefix + " " + message;
        player.sendMessage(message);
    }
}
