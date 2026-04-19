package org.senbler.adminUtilities;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.senbler.adminUtilities.commands.*;
import org.senbler.adminUtilities.listeners.*;
import org.senbler.adminUtilities.menus.Menus;
import org.senbler.adminUtilities.npc.npc;
import org.senbler.adminUtilities.npc.npcFile;
import org.senbler.adminUtilities.npc.npcListener;

import java.util.ArrayList;

public final class AdminUtilities extends JavaPlugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        plugin = this;
        Menus.initialize();
        npcFile.readNPCs();
        getLogger().info("[AdminUtilities] Loaded " + npcFile.getNpcs().size() + " npc(s).");
        for (npc npc : npcFile.getNpcs()) {
            npc.respawn();
        }

        getCommand("adminutilities").setExecutor(new AdminUtilitiesCommand());
        getCommand("freeze").setExecutor(new FreezeCommand());
        getCommand("unfreeze").setExecutor(new UnfreezeCommand());

        getServer().getPluginManager().registerEvents(new Menus(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new npcListener(), this);

        getLogger().info("[AdminUtilities] Enabled");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        saveConfig();
        getLogger().info("[AdminUtilities] Saving " + npcFile.getNpcs().size() + " npc(s).");
        npcFile.saveNPCs();
        for (npc n : npcFile.getNpcs()) {
            n.remove();
        }
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
