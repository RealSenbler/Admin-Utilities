package org.senbler.adminUtilities.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.function.Consumer;

public class testMenu implements Listener {

    /*private GUI gui;
    private Inventory inventory;
    private HashMap<Integer, Consumer<Player>> functions;

    public testMenu (GUI gui) {
        this.gui = gui;
        this.inventory = gui.getInventory();
        this.functions = gui.getFunctions();
    }*/

    /*@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != gui.getInventory()) {
            return;
        }
        event.setCancelled(true);
        Consumer<Player> action = functions.get(event.getRawSlot());
        if (action != null) {
            action.accept((Player) event.getWhoClicked());
        }
    }*/
}
