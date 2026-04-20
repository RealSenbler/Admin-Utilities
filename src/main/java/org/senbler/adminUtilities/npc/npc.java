package org.senbler.adminUtilities.npc;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitScheduler;
import org.checkerframework.checker.units.qual.N;
import org.senbler.adminUtilities.AdminUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

public class npc {

    private String name;
    private ArrayList<String> dialog;
    private EntityType mobType;
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;
    private UUID entityId;
    private transient Entity entity;
    private transient boolean isSendingDialog = false;

    public npc(String name, EntityType entityType) {
        this.name = name;
        this.mobType = entityType;
        this.dialog = new ArrayList<>();

    }

//    public void spawnNPC(Player p) {
//        Location loc = p.getLocation();
//        entity = loc.getWorld().spawnEntity(loc, mobType);
//
//        entity.setCustomName(this.name);
//        entity.setCustomNameVisible(true);
//        entity.setMetadata("npc", new FixedMetadataValue(AdminUtilities.getPlugin(), true));
//        if (entity instanceof LivingEntity living) {
//            living.setInvulnerable(true);
//            living.setAI(false);
//            living.setRemoveWhenFarAway(false);
//            living.setPersistent(true);
//            living.setSilent(true);
//
//        }
//        this.location = entity.getLocation();
//    }
    public void spawnNPC(Player p) {
        spawnAt(p.getLocation());
    }

    public void spawnNPC(Location loc) {
        spawnAt(loc);
    }

    private void spawnAt(Location loc) {
        loc = new Location(loc.getWorld(), ((int)loc.getBlockX())+.5, (int)loc.getBlockY(), ((int)loc.getBlockZ())+.5);
        entity = loc.getWorld().spawnEntity(loc, mobType);
        entityId = entity.getUniqueId();
        configureEntity();
        storeLocation(entity.getLocation());
    }

    private void configureEntity() {
        entity.setCustomName(this.name);
        entity.setCustomNameVisible(true);
        entity.setMetadata("npc", new FixedMetadataValue(AdminUtilities.getPlugin(), true));
        if (entity instanceof LivingEntity living) {
            living.setInvulnerable(true);
            living.setAI(false);
            living.setRemoveWhenFarAway(false);
            living.setSilent(true);
            living.setCanPickupItems(false);
            EntityEquipment eq = living.getEquipment();
            if (eq != null) {
                eq.clear();
            }
        }
        if (entity instanceof Mannequin mannequin) {
            mannequin.setDescription(null);
            mannequin.setImmovable(true);
        }
    }
    private void storeLocation(Location loc) {
        this.worldName = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    private Location buildLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }
    public Location getLocation() {
        return buildLocation();
    }
    public Entity getEntity() {
        return entity;
    }

    public void addDialog(String dialog) {
        this.dialog.add(dialog);
    }
    public void removeDialog() {
        this.dialog.removeLast();
    }
    public void clearDialog() {
        this.dialog.clear();
    }
    public ArrayList<String> getDialog() {
        return this.dialog;
    }

    public void setName(String name) {
        this.name = name;
        if (this.entity != null) {
            this.entity.setCustomName(name);
        }
    }
    public String getName () {
        return this.name;
    }

    public void setMobType(EntityType mobType) {
        this.mobType = mobType;
        if (entity != null) {
            entity.remove();
            spawnAt(buildLocation());
        }
    }
    public UUID getUniqueId() {
        return entity != null ? entity.getUniqueId() : null;
    }
    public void sendDialog(Player p) {
        if (isSendingDialog) return;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        Plugin plugin = AdminUtilities.getPlugin();
        isSendingDialog = true;
        for (int i = 0; i < dialog.size(); i++) {
            final String line = dialog.get(i);
            scheduler.runTaskLater(plugin,
                    () ->
                    {
                        p.sendMessage("§7[§e"+ name + "§7] " + line);
                        if ((int)Math.random()*2 == 0) {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 5);
                        } else {
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1, 5);
                        }
                    }
                    ,
                    i * 20L);
        }
        isSendingDialog = false;
    }
    public void remove () {
        if (entity != null) entity.remove();
    }
    public void respawn () {
        Location loc = buildLocation();
        if (loc == null) return;

        loc.getChunk().load(true);

        Entity existing = (entityId != null) ? Bukkit.getEntity(entityId) : null;
        if (existing != null && !existing.isDead() && existing.getType() == mobType) {
            this.entity = existing;
            existing.teleport(loc);
            configureEntity();
            return;
        }

        for (Entity e : loc.getWorld().getNearbyEntities(loc, 2, 3, 2)) {
            if (e.getType() == mobType && name.equals(e.getCustomName())) {
                e.remove();
            }
        }

        spawnAt(loc);
    }
    public ItemStack getNPCEgg () {
        String entityName = this.mobType.getName().toUpperCase();
        String eggName = entityName + "_SPAWN_EGG";
        Material egg = Material.getMaterial(eggName);
        if (egg == null) egg = Material.EGG;
        ItemStack item = new ItemStack(egg, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(this.name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7NPC Type: " + entityName);
        lore.add("");
        lore.add("§7Dialog: ");
        lore.addAll(this.dialog);
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(AdminUtilities.getPlugin(), "metadata"), PersistentDataType.STRING, "npcegg");
        item.setItemMeta(meta);
        return item;
    }

    public boolean isMannequin () {
        if (entity == null) return false;
        if (entity instanceof Mannequin) {
            return true;
        }
        return false;
    }
    public boolean isVillager () {
        if (entity == null) return false;
        if (entity instanceof Villager) {
            return true;
        }
        return false;
    }

    public void setSkin (String skinUsername) {
        if (!isMannequin()) {
            throw new IllegalStateException("Mannequin has not been set");
        }
        Mannequin mannequin = (Mannequin) entity;
        PlayerProfile profile = Bukkit.createPlayerProfile(skinUsername);
        mannequin.setProfile(ResolvableProfile.resolvableProfile((com.destroystokyo.paper.profile.PlayerProfile) profile));
    }

    @Override
    public String toString() {
        return "npc{" +
                "name='" + name + '\'' +
                ", dialog=" + dialog +
                ", mobType=" + mobType +
                ", worldName='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", entity=" + entity +
                '}';
    }
}
