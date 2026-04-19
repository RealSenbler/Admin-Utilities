package org.senbler.adminUtilities.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.senbler.adminUtilities.AdminUtilities;

import static org.senbler.adminUtilities.commands.FreezeCommand.getFrozenLocation;
import static org.senbler.adminUtilities.commands.FreezeCommand.isFrozen;

public class FreezeListener implements Listener {

    private AdminUtilities plugin;

    public FreezeListener(AdminUtilities plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!isFrozen(p)) {
            return;
        }
        if (AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-camera-movement")) {
            Location newLoc = e.getTo();
            if (newLoc.getX() == getFrozenLocation(p).getX() && newLoc.getZ() == getFrozenLocation(p).getZ() && newLoc.getY() == getFrozenLocation(p).getY()) {
                return;
            }
        }
        e.setCancelled(true);
        p.sendTitle("§7You Are §bFrozen§", "§7You cannot move while frozen.", 2, 10, 2);
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (plugin.getConfig().getBoolean("freeze-settings.allow-interact")) {
            return;
        }
        Player p = e.getPlayer();
        if (!isFrozen(p)) {
            return;
        }
        e.setCancelled(true);
        p.sendTitle("§7You Are §bFrozen", "§7You cannot interact while frozen.", 2, 10, 2);
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (plugin.getConfig().getBoolean("freeze-settings.allow-chat")) {
            return;
        }
        Player p = e.getPlayer();
        if (!isFrozen(p)) {
            return;
        }
        e.setCancelled(true);
        p.sendMessage("§7You are §bfrozen§7. You cannot chat while frozen.");
    }
}
