package org.senbler.adminUtilities.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.senbler.adminUtilities.AdminUtilities;

import java.util.*;

import static org.bukkit.Bukkit.getWorld;

public class FreezeCommand implements CommandExecutor {

    private static final Set<UUID> frozenPlayers = new HashSet<>();
    private static final Map<UUID, Location> frozenLocations = new HashMap<>();

    public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
        List<Player> playersToFreeze = new ArrayList<>();
        if (args.length == 0) {
            AdminUtilities.sendMessage((Player)sender, "You must enter the name of a player or players to §bfreeze§7.");
            return false;
        }
        for (String s : args) {
            Player p = Bukkit.getPlayer(s);
            if (p == null) {
                AdminUtilities.sendMessage((Player)sender, "Player \"" + s + "\" does not exist.");
            } else if (isFrozen(p)){
                AdminUtilities.sendMessage((Player)sender, "Player \"" + p.getName() + "\" is already §bfrozen.");
            } else {
                playersToFreeze.add(p);
            }
        }
        for (Player p : playersToFreeze) {
            if (AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.teleport-player-to-ground")) {
                Location playerloc = p.getLocation();
                Block highest = playerloc.getWorld().getHighestBlockAt(playerloc);
                Location ground = highest.getLocation().add(0, 1, 0);
                p.teleport(ground);
            }
            setFrozen(p, true);
            p.sendMessage("§7You have been §bfrozen§7.");
            AdminUtilities.sendMessage((Player)sender, "§bFroze§7 " + p.getName() + ".");
        }
        return true;
    }
    public static void setFrozen(Player player, boolean frozen) {
        if (frozen) {
            frozenPlayers.add(player.getUniqueId());
            frozenLocations.put(player.getUniqueId(), player.getLocation());
        } else {
            frozenPlayers.remove(player.getUniqueId());
            frozenLocations.remove(player.getUniqueId());
        }
    }
    public static boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    public static Location getFrozenLocation(Player player) {
        return frozenLocations.get(player.getUniqueId());
    }
}
