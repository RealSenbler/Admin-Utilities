package org.senbler.adminUtilities.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.senbler.adminUtilities.AdminUtilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;



public class Menus implements Listener {

    private static final HashMap<String, GUI> guis = new HashMap<>();
    private static final HashMap<UUID, GUI> openGUIs = new HashMap<>();
    private static final Map<UUID, Consumer<String>> awaitingInput = new HashMap<>();

    public static GUI getGUI(String key) {
        return guis.get(key);
    }

    public static void initialize() {


        //Main Menu
        {
            GUI mainMenu = new GUI("§cAdmin Utilities", 3);
            mainMenu.setOnCreate((player, gui) -> {
                mainMenu.fillEdge(null);
                mainMenu.setItem(22, mainMenu.createItem(Material.BARRIER, "§cExit", "§7Click to exit the menu."), (event, _) -> {
                    event.getWhoClicked().closeInventory();
                });
                mainMenu.setItem(10, mainMenu.createItem(Material.SKELETON_SKULL, "§eWhitelisted Players", "§7Opens a menu of whitelisted players."), (event, _) -> {
                    openGUI((Player) event.getWhoClicked(), "whitelist_menu");
                });
                mainMenu.setItem(11, mainMenu.createItem(Material.ANVIL, "§eBanned Players", "§7Opens a menu of banned players."), (event, _) -> {
                    openGUI((Player) event.getWhoClicked(), "ban_menu");
                });
                mainMenu.setItem(12, mainMenu.createItem(Material.CRAFTING_TABLE, "§eItem Creator", "§7Opens an item creator menu.", "§7Currently implemented features:", "§7- Name", "§7- Lore", "§7- Glow", "§7- Material"), (event, _) -> {
                    openGUI((Player) event.getWhoClicked(), "item_creator");
                });
            });
            guis. put("main_menu", mainMenu);
        }
        //Whitelist Menu
        {
            GUI whitelistMenu = new GUI("§3Whitelists", 6);
            whitelistMenu.setOnCreate((player2, gui) -> {
                if (!Bukkit.isWhitelistEnforced()){
                    AdminUtilities.sendMessage(player2, "Reminder: Whitelist is not enabled!");
                    player2.closeInventory();

                }
                if (!(Bukkit.getWhitelistedPlayers().size() > 28)) {
                    AtomicInteger i = new AtomicInteger(10);
                    gui.nuke();
                    gui.fillEdge(null);
                    gui.setItem(49, gui.createItem(Material.BARRIER, "§cExit", "§7Click to exit the menu."), (event, _) -> {
                        event.getWhoClicked().closeInventory();

                    });
                    gui.setItem(48, gui.createItem(Material.ARROW, "§aBack", null), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        openGUI(player, "main_menu");
                    });
                    // Plus Button
                        ItemStack plusButton = new ItemStack(Material.PLAYER_HEAD, 1);
                        SkullMeta skullMeta = (SkullMeta) plusButton.getItemMeta();
                        PlayerProfile playerProfile = Bukkit.createProfile("plusbutton");
                        PlayerTextures textures = playerProfile.getTextures();
                        try {
                            textures.setSkin(new URL("http://textures.minecraft.net/texture/177bb66fc73a97cefcb3a4bfdccb12281f44dd326ccd0ff39d47e985bfeff343"));
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                        playerProfile.setTextures(textures);
                        skullMeta.setPlayerProfile(playerProfile);
                        skullMeta.setDisplayName("§aAdd a player to the whitelist");
                        skullMeta.setLore(Arrays.asList("§7Click to add a new player to the whitelist."));
                        plusButton.setItemMeta(skullMeta);
                        gui.setItem(51, plusButton, (event, _) -> {
                           Player player = (Player) event.getWhoClicked();
                           player.closeInventory();
                           player.sendTitle("§a§lInput", "§7Enter the username in chat", 5, 80, 5);
                            awaitingInput.put(player.getUniqueId(), input -> {
                                AdminUtilities.sendMessage(player, "§a" + input + " was added to the whitelist!");
                                OfflinePlayer pl = Bukkit.getOfflinePlayer(input);
                                if (pl != null && !pl.isWhitelisted()) {
                                    pl.setWhitelisted(true);
                                }
                                openGUI(player, "whitelist_menu");
                            });
                        });

                    Bukkit.getWhitelistedPlayers().stream().forEach(offlinePlayer -> {
//                        Bukkit.getLogger().info("Player " + offlinePlayer.getName());
                        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                        SkullMeta meta = (SkullMeta)item.getItemMeta();
                        PlayerProfile profile = offlinePlayer.getPlayerProfile();
                        if (!profile.isComplete()) {
                            profile.complete();
                        }
                        meta.lore(List.of(
                                Component.text("Right click to ").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                                        .append(Component.text("remove").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                                        .append(Component.text(" player from the whitelist.").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                        ));
                        meta.displayName(Component.text(offlinePlayer.getName(), NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false));
                        meta.setOwningPlayer(offlinePlayer);
                        item.setItemMeta(meta);
                        whitelistMenu.setItem(i.get(), item, (event, _) -> {
                            if (!event.getClick().isRightClick()) {
                                return;
                            }
                            offlinePlayer.setWhitelisted(false);
                            AdminUtilities.sendMessage((Player)event.getWhoClicked(), "§ePlayer removed from whitelist!");
                            Bukkit.getScheduler().runTask(AdminUtilities.getPlugin(), () -> {
                                //whitelistMenu.open((Player)event.getWhoClicked());
                                openGUI((Player)event.getWhoClicked(), "whitelist_menu");
                            });
                            //whitelistMenu.open((Player)event.getWhoClicked());
                        });
                        i.getAndIncrement();
                    });
                } else {
                    AdminUtilities.sendMessage(player2, "This feature is not made for whitelist sizes over 28 :( sorry!");
                }
            });
            guis.put("whitelist_menu", whitelistMenu);
        }

        //Item Creator
        {
            GUI itemCreator = new GUI("Item Creator", 5);
            final String[] title = {"§eCustom Item"};
            final Material[] itemMaterial = {Material.BARRIER};
            final ArrayList<String> lore = new ArrayList<>();
            lore.add("§7This is your custom item!");
            final boolean[] glow = {false};
            int amount = 1;
            itemCreator.setOnCreate((_, gui) -> {
                itemCreator.fillInventory(null);

                ItemStack customItem = new ItemStack(itemMaterial[0], amount);
                ItemMeta customItemMeta = customItem.getItemMeta();
                customItemMeta.setDisplayName(title[0]);
                customItemMeta.setLore(lore);
                customItemMeta.setEnchantmentGlintOverride(glow[0]);
                customItem.setItemMeta(customItemMeta);
                itemCreator.setItem(24, customItem, null);

                itemCreator.setItem(40, itemCreator.createItem(Material.BARRIER, "§cExit", "§7Exits the menu."), (event, _) -> {
                    event.getWhoClicked().closeInventory();
                });
                itemCreator.setItem(39, itemCreator.createItem(Material.ARROW, "§aBack", null), (event, _) -> {
                    openGUI((Player)event.getWhoClicked(), "main_menu");
                });
                itemCreator.setItem(10, itemCreator.createItem(Material.NAME_TAG, "§eItem Name", "§7Sets the name of the item.", "§7Use \"&\" for color codes."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.sendTitle("§a§lInput", "§7Enter the name in chat", 5, 80, 5);
                    awaitingInput.put(player.getUniqueId(), input -> {
                        title[0] = input.replace("&", "§");
                        ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                        ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                        tempcustomItemMeta.setDisplayName(title[0]);
                        tempcustomItemMeta.setLore(lore);
                        tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                        tempcustomItem.setItemMeta(tempcustomItemMeta);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    });
                });
                itemCreator.setItem(11, itemCreator.createItem(Material.GRASS_BLOCK, "§eChange Material", "§7Changes the material of the item.", "", "§7Material Format:", "", "§7- GRASS_BLOCK", "§7- grass block"), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.sendTitle("§a§lInput", "§7Enter the material in chat", 5, 80, 5);
                    awaitingInput.put(player.getUniqueId(), input -> {
                        input = input.trim().toUpperCase().replace(" ", "_");
                        if (Material.getMaterial(input) != null) {
                            itemMaterial[0] = Material.getMaterial(input);
                        } else {
                            AdminUtilities.sendMessage(player, "The entered input: " + input + " is not a valid material.");
                            AdminUtilities.sendMessage(player, "The correct format is either \"GRASS_BLOCK\" or \"grass block\".");
                        }
                        ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                        ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                        tempcustomItemMeta.setDisplayName(title[0]);
                        tempcustomItemMeta.setLore(lore);
                        tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                        tempcustomItem.setItemMeta(tempcustomItemMeta);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    });
                });
                itemCreator.setItem(12, itemCreator.createItem(Material.END_CRYSTAL, "§eSet glow", "§7Click to set if the item is glowing"),  (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    glow[0] = !glow[0];
                    ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                    ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                    tempcustomItemMeta.setDisplayName(title[0]);
                    tempcustomItemMeta.setLore(lore);
                    tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                    tempcustomItem.setItemMeta(tempcustomItemMeta);
                    itemCreator.setItem(24, customItem, null);
                    openGUI(player, "item_creator");
                });
                ArrayList<String> tempLore = new ArrayList<>();
                tempLore.add(0, "§7Use \"&\" for color codes.");
                tempLore.add("");
                for (String s : lore) {
                    tempLore.add(s);
                }
                String[] loreArray = tempLore.toArray(new String[tempLore.size()]);
                itemCreator.setItem(19, itemCreator.createItem(Material.BOOK, "§eItem Lore", loreArray), null);
                itemCreator.setItem(20, itemCreator.createSkullItemStack(1, "http://textures.minecraft.net/texture/5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1", "§aAdd to lore", "§7Adds to the lore of the item."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.sendTitle("§a§lInput", "§7Enter the line in chat", 5, 80, 5);
                    awaitingInput.put(player.getUniqueId(), input -> {
                        lore.add(input.replace("&", "§"));
                        ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                        ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                        tempcustomItemMeta.setDisplayName(title[0]);
                        tempcustomItemMeta.setLore(lore);
                        tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                        tempcustomItem.setItemMeta(tempcustomItemMeta);
                        itemCreator.setItem(24, customItem, null);
                        ArrayList<String> tempLore2 = new ArrayList<>();
                        tempLore2.add(0, "§7Use \"&\" for color codes.");
                        tempLore2.add("");
                        for (String s : lore) {
                            tempLore2.add(s);
                        }
                        String[] loreArray2 = tempLore2.toArray(new String[tempLore2.size()]);
                        itemCreator.setItem(19, itemCreator.createItem(Material.BOOK, "§eItem Lore", loreArray2), null);
                        openGUI(player, "item_creator");
                    });
                });
                itemCreator.setItem(21, itemCreator.createSkullItemStack(1, "http://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46", "§cRemove last line", "§7Removes the last line of lore."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    if (lore.size() > 0) {
                        lore.remove(lore.size() - 1);
                        ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                        ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                        tempcustomItemMeta.setDisplayName(title[0]);
                        tempcustomItemMeta.setLore(lore);
                        tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                        tempcustomItem.setItemMeta(tempcustomItemMeta);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    } else {
                        AdminUtilities.sendMessage(player, "The lore is already empty!");
                    }
                });
                itemCreator.setItem(22, itemCreator.createItem(Material.FLINT_AND_STEEL, "§eClear Lore", "§7Clears every line of the lore."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    if (lore.size() > 0) {
                        lore.clear();
                        ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                        ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                        tempcustomItemMeta.setDisplayName(title[0]);
                        tempcustomItemMeta.setLore(lore);
                        tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                        tempcustomItem.setItemMeta(tempcustomItemMeta);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    } else {
                        AdminUtilities.sendMessage(player, "The lore is already empty!");
                    }
                });
                itemCreator.setItem(42, itemCreator.createItem(Material.CHEST, "§aGive item", "§7Click to give yourself this item."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    ItemStack tempcustomItem = new ItemStack(itemMaterial[0], amount);
                    ItemMeta tempcustomItemMeta = customItem.getItemMeta();
                    tempcustomItemMeta.setDisplayName(title[0]);
                    tempcustomItemMeta.setLore(lore);
                    tempcustomItemMeta.setEnchantmentGlintOverride(glow[0]);
                    tempcustomItem.setItemMeta(tempcustomItemMeta);
                    player.give(tempcustomItem);
                });
            });
            guis.put("item_creator", itemCreator);
        }

        //Bans Menu
        {
            GUI bansMenu = new GUI("§cBans", 6);
            bansMenu.setOnCreate((player2, gui) -> {
                if (Bukkit.getBannedPlayers().isEmpty()) {
                    AdminUtilities.sendMessage(player2, "Reminder: There are no banned players.");
                    player2.closeInventory();
                }
                if (!(Bukkit.getWhitelistedPlayers().size() > 28)) {
                    AtomicInteger i = new AtomicInteger(10);
                    gui.nuke();
                    gui.fillEdge(null);
                    gui.setItem(49, gui.createItem(Material.BARRIER, "§cExit", "§7Click to exit the menu."), (event, _) -> {
                        event.getWhoClicked().closeInventory();

                    });
                    gui.setItem(48, gui.createItem(Material.ARROW, "§aBack", null), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        openGUI(player, "main_menu");
                    });
//                    // Plus Button
//                    ItemStack plusButton = new ItemStack(Material.PLAYER_HEAD, 1);
//                    SkullMeta skullMeta = (SkullMeta) plusButton.getItemMeta();
//                    PlayerProfile playerProfile = Bukkit.createProfile("plusbutton");
//                    PlayerTextures textures = playerProfile.getTextures();
//                    try {
//                        textures.setSkin(new URL("http://textures.minecraft.net/texture/177bb66fc73a97cefcb3a4bfdccb12281f44dd326ccd0ff39d47e985bfeff343"));
//                    } catch (MalformedURLException e) {
//                        throw new RuntimeException(e);
//                    }
//                    playerProfile.setTextures(textures);
//                    skullMeta.setPlayerProfile(playerProfile);
//                    skullMeta.setDisplayName("§aAdd a player to the whitelist");
//                    skullMeta.setLore(Arrays.asList("§7Click to add a new player to the whitelist."));
//                    plusButton.setItemMeta(skullMeta);
//                    gui.setItem(51, plusButton, (event, _) -> {
//                        Player player = (Player) event.getWhoClicked();
//                        player.closeInventory();
//                        player.sendTitle("§a§lInput", "§7Enter the username in chat", 5, 80, 5);
//                        awaitingInput.put(player.getUniqueId(), input -> {
//                            AdminUtilities.sendMessage(player, "§a" + input + " was added to the whitelist!");
//                            OfflinePlayer pl = Bukkit.getOfflinePlayer(input);
//                            if (pl != null && !pl.isWhitelisted()) {
//                                pl.setWhitelisted(true);
//                            }
//                            openGUI(player, "ban_menu");
//                        });
//                    });

                    Bukkit.getBannedPlayers().stream().forEach(offlinePlayer -> {
                        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                        SkullMeta meta = (SkullMeta)item.getItemMeta();
                        PlayerProfile profile = offlinePlayer.getPlayerProfile();
                        if (!profile.isComplete()) {
                            profile.complete();
                        }
                        meta.setLore(Arrays.asList("§7Ban reason: §c" + ((Bukkit.getBanList(BanList.Type.NAME).getBanEntry(offlinePlayer.getName()).getReason()) == null ? "none" : (Bukkit.getBanList(BanList.Type.NAME).getBanEntry(offlinePlayer.getName()).getReason())), "§7Right click to §aunban§7 this player."));
                        meta.displayName(Component.text(offlinePlayer.getName(), NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false));
                        meta.setOwningPlayer(offlinePlayer);
                        item.setItemMeta(meta);
                        bansMenu.setItem(i.get(), item, (event, _) -> {
                            if (!event.getClick().isRightClick()) {
                                return;
                            }
                            Bukkit.getBanList(BanList.Type.NAME).pardon(offlinePlayer.getName());
                            AdminUtilities.sendMessage((Player)event.getWhoClicked(), "§ePlayer unbanned!");
                            Bukkit.getScheduler().runTask(AdminUtilities.getPlugin(), () -> {
                                //whitelistMenu.open((Player)event.getWhoClicked());
                                openGUI((Player)event.getWhoClicked(), "ban_menu");
                            });
                            //whitelistMenu.open((Player)event.getWhoClicked());
                        });
                        i.getAndIncrement();
                    });
                } else {
                    AdminUtilities.sendMessage(player2, "This feature is not yet made for ban list sizes over 28 :( sorry!");
                }
            });
            guis.put("ban_menu", bansMenu);
        }
    }

    public static GUI openGUI(Player player, String key) {
        GUI gui = getGUI(key);
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
        return gui;
    }

    public GUI getActiveGUI(Player player) {
        return openGUIs.get(player.getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = awaitingInput.remove(player.getUniqueId());

        if (callback != null) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText()
                    .serialize(event.message());
            Bukkit.getScheduler().runTask(AdminUtilities.getPlugin(), () -> callback.accept(message));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Player player =  (Player) event.getWhoClicked();
        if((!event.getView().getTopInventory().equals(event.getInventory()) && openGUIs.containsKey(player.getUniqueId())) || !openGUIs.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        //boolean test_hasAccepted = false;
        //checks if the clicked inventory was NOT the top inventory and the player has an inventory open.
        if((!event.getView().getTopInventory().equals(event.getClickedInventory()) && openGUIs.containsKey(player.getUniqueId())) || !openGUIs.containsKey(player.getUniqueId())) return;
        //cancels all in-menu clicks to prevent picking up items.
        event.setCancelled(true);
        //gets the open gui of the player who clicked.
        GUI gui = getActiveGUI(player);
        //if the item at the clicked slot has a consumer, run it.
        if(gui.getFunction(event.getSlot()) != null) {
            gui.getFunction(event.getSlot()).run(event, gui);
            //test_hasAccepted = true;
        }
        //temporary debug logging.
        //Bukkit.getLogger().info(test_hasAccepted ? "Consumer accepted at slot: " + event.getSlot() : "Consumer rejected at slot: " + event.getSlot());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }
}
