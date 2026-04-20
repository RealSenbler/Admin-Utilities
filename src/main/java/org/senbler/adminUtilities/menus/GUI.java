package org.senbler.adminUtilities.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class GUI {

    private ItemStack[][] gui;
    private String name;
    private Inventory inventory;
    private int rows;
    private HashMap<Integer, onClick> functions;
    private onCreate onCreate;

    public GUI (String name, int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6.");
        }
        this.name = name;
        this.rows = rows;
        this.gui = new ItemStack[rows][9];
        this.functions = new HashMap<>();
        inventory = createInventory(rows);
    }

    public ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack createItem(Material material, String name) {
        return createItem(material, name, (String[]) null);
    }
    public ItemStack createItem(Material material, int amount, String name, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    public ItemStack createSkullItemStack(int amount, String url, String name, String... lore) {
        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createProfile("skullItem");
        PlayerTextures textures = playerProfile.getTextures();
        try {
            textures.setSkin(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        playerProfile.setTextures(textures);
        skullMeta.setPlayerProfile(playerProfile);
        skullMeta.setDisplayName(name);
        skullMeta.setLore(Arrays.asList(lore));
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

    // null for default material which is gray glass panes
    public void fillEdge (Material edgeMaterial) {
        if (edgeMaterial == null) {
            edgeMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }
        for (int i = 0; i < 9; i++) {
            gui[0][i] = createItem(edgeMaterial, " ", null);
        }
        for (int i = 0; i < 9; i++) {
            gui[gui.length-1][i] = createItem(edgeMaterial, " ", null);
        }
        for (int i = 0; i < gui.length; i++) {
            gui[i][0] = createItem(edgeMaterial, " ", null);
        }
        for (int i = 0; i < gui.length; i++) {
            gui[i][8] = createItem(edgeMaterial, " ", null);
        }
        update();
    }

    public void fillInventory (Material material) {
        if (material == null) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }
        for (int i = 0; i < rows*9; i++) {
            inventory.setItem(i, createItem(material, " ", null));
        }
    }

    public void setItem (int slot, ItemStack item, onClick function) {
        int row = slot / 9;
        int col = slot % 9;
        gui[row][col] = item;
        inventory.setItem(slot, item);
        functions.put(slot, function);
    }

    public void setItems(ItemStack item, int... slots) {
        for (int slot : slots) {
            setItem(slot, item, null);
        }
    }

    public void nuke() {
        for(int i = 0; i < rows * 9; i++) {
            setItem(i, null, null);
        }
    }

    private Inventory createInventory(int rows) {
        Inventory inventory = Bukkit.createInventory(null, rows * 9, name);
        for (int row = 0; row < gui.length; row++) {
            for (int col = 0; col < 9; col++) {
                if (gui[row][col] != null) {
                    inventory.setItem(row * 9 + col, gui[row][col]);
                }
            }
        }
        return inventory;
    }

    public void open (Player player) {
        getOnCreate().run(player, this);
        player.openInventory(inventory);
    }

    public void update () {
        inventory = createInventory(rows);
    }

    public void update (Player player) {
        inventory = createInventory(rows);
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public boolean hasFunction(int i) {
        return functions.containsKey(i);
    }

    public onClick getFunction(int i) {
        return functions.get(i);
    }

    public HashMap<Integer, onClick> getFunctions() {
        return functions;
    }

    public void setOnCreate(onCreate onCreate) {
        this.onCreate = onCreate;
    }

    public onCreate getOnCreate() {
        return onCreate;
    }

    @FunctionalInterface
    public interface onCreate {
        void run(Player player, GUI gui);
    }

    @FunctionalInterface
    public interface onClick {
        void run(InventoryClickEvent event, GUI gui);
    }
}
