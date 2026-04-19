package org.senbler.adminUtilities.npc;

import io.papermc.paper.entity.LookAnchor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.senbler.adminUtilities.AdminUtilities;
import org.senbler.adminUtilities.menus.Menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class npcListener implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 1000;

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Player p = e.getPlayer();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            long cooldown = cooldowns.get(uuid);
            if (now - cooldown < COOLDOWN_MS) {
                return;
            }
        }
        if (e.getRightClicked().hasMetadata("npc")) {
            for (npc npc : npcFile.getNpcs()) {
                if (e.getRightClicked().getUniqueId().equals(npc.getUniqueId())) {
                    if (p.hasPermission("AdminUtilities.edit_npc") && p.isSneaking()) {
                        if (npc == null) continue;
                        Menus.openNPCEditor(e.getPlayer(), npc);
                    } else {
                        npc.sendDialog(p);
                    }
                }
            }
        }
        cooldowns.put(uuid, System.currentTimeMillis());
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location playerLocation = p.getLocation();
        ArrayList<npc> npcs = npcFile.getNpcs();
        if (npcs.isEmpty()) {
            return;
        }
        for (npc npc : npcs) {
            if (playerLocation.distanceSquared(npc.getLocation()) < 5*5) {
                double x = playerLocation.getX();
                double y = playerLocation.getY()+1;
                double z = playerLocation.getZ();
                npc.getEntity().lookAt(x, y, z, LookAnchor.EYES);
            }
        }
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().hasMetadata("npc")) {
            e.setCancelled(true);
            Player player = (Player)e.getDamageSource().getCausingEntity();
            if (player == null) return;
            for (npc npc : npcFile.getNpcs()) {
                if (e.getEntity().getUniqueId().equals(npc.getUniqueId())) {
                    npc.sendDialog(player);
                }
            }

        }
    }
    @EventHandler
    public void onEntityCombust(EntityCombustEvent e) {
        if (e.getEntity().hasMetadata("npc")) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta.getPersistentDataContainer().get(new NamespacedKey(AdminUtilities.getPlugin(), "metadata"), PersistentDataType.STRING).equals("npcegg")) {
            e.setCancelled(true);
            Location location = e.getInteractionPoint().getBlock().getLocation();
            location = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ());
            Menus.makingNPC.spawnNPC(location);
            npcFile.addNpc(Menus.makingNPC);
            p.setItemInHand(null);
        } else {
            return;
        }
    }
}
