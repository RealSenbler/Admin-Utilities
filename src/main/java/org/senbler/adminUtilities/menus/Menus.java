package org.senbler.adminUtilities.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
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
import org.senbler.adminUtilities.npc.npc;
import org.senbler.adminUtilities.npc.npcFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;



public class Menus implements Listener {

    private static final HashMap<String, GUI> guis = new HashMap<>();
    private static final HashMap<UUID, GUI> openGUIs = new HashMap<>();
    private static final Map<UUID, Consumer<String>> awaitingInput = new HashMap<>();
    private static npc editingNPC = null;
    public static npc makingNPC = new npc("§aNew NPC", EntityType.ZOMBIE);


    public static GUI getGUI(String key) {
        return guis.get(key);

    }

    public static void initialize() {
        makingNPC.addDialog("§7Great Dialog");

        //Main Menu
        {
            GUI mainMenu = new GUI("§cAdmin Utilities", 3);
            mainMenu.setOnCreate((_, _) -> {
                mainMenu.fillEdge(null);
                mainMenu.setItem(22, mainMenu.createItem(Material.BARRIER, "§cExit", "§7Click to exit the menu."), (event, _) -> event.getWhoClicked().closeInventory());
                mainMenu.setItem(21, mainMenu.createItem(Material.COMPARATOR, "§ePlugin Settings", "§7Opens the plugin settings menu."), (event, _) -> openGUI((Player)event.getWhoClicked(), "settings_menu"));
                mainMenu.setItem(10, mainMenu.createItem(Material.SKELETON_SKULL, "§eWhitelisted Players", "§7Opens a menu of whitelisted players."), (event, _) -> openGUI((Player) event.getWhoClicked(), "whitelist_menu"));
                mainMenu.setItem(11, mainMenu.createItem(Material.ANVIL, "§eBanned Players", "§7Opens a menu of banned players."), (event, _) -> openGUI((Player) event.getWhoClicked(), "ban_menu"));
                mainMenu.setItem(12, mainMenu.createItem(Material.CRAFTING_TABLE, "§eItem Creator", "§7Opens an item creator menu.", "§7Currently implemented features:", "§7- Name", "§7- Lore", "§7- Glow", "§7- Material"), (event, _) -> openGUI((Player) event.getWhoClicked(), "item_creator"));
                mainMenu.setItem(13, mainMenu.createItem(Material.VILLAGER_SPAWN_EGG, "§eNPC Creator", "§7Opens the NPC creator menu."), (event, _) -> openGUI((Player) event.getWhoClicked(), "npc_creator"));
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
                    gui.setItem(49, gui.createItem(Material.BARRIER, "§cExit", "§7Click to exit the menu."), (event, _) -> event.getWhoClicked().closeInventory());
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
            itemCreator.setOnCreate((_, _) -> {
                itemCreator.fillInventory(null);

                ItemStack customItem = new ItemStack(itemMaterial[0], amount);
                ItemMeta customItemMeta = customItem.getItemMeta();
                customItemMeta.setDisplayName(title[0]);
                customItemMeta.setLore(lore);
                customItemMeta.setEnchantmentGlintOverride(glow[0]);
                customItem.setItemMeta(customItemMeta);
                itemCreator.setItem(24, customItem, null);

                itemCreator.setItem(40, itemCreator.createItem(Material.BARRIER, "§cExit", "§7Exits the menu."), (event, _) -> event.getWhoClicked().closeInventory());
                itemCreator.setItem(39, itemCreator.createItem(Material.ARROW, "§aBack"), (event, _) -> openGUI((Player)event.getWhoClicked(), "main_menu"));
                itemCreator.setItem(10, itemCreator.createItem(Material.NAME_TAG, "§eItem Name", "§7Sets the name of the item.", "§7Use \"&\" for color codes."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.sendTitle("§a§lInput", "§7Enter the name in chat", 5, 80, 5);
                    awaitingInput.put(player.getUniqueId(), input -> {
                        title[0] = input.replace("&", "§");
                        ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
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
                        ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    });
                });
                itemCreator.setItem(12, itemCreator.createItem(Material.END_CRYSTAL, "§eSet glow", "§7Click to set if the item is glowing"),  (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    glow[0] = !glow[0];
                    ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
                    itemCreator.setItem(24, customItem, null);
                    openGUI(player, "item_creator");
                });
                ArrayList<String> tempLore = new ArrayList<>();
                tempLore.addFirst("§7Use \"&\" for color codes.");
                tempLore.add("");
                tempLore.addAll(lore);
                String[] loreArray = tempLore.toArray(new String[tempLore.size()]);
                itemCreator.setItem(19, itemCreator.createItem(Material.BOOK, "§eItem Lore", loreArray), null);
                itemCreator.setItem(20, itemCreator.createSkullItemStack(1, "http://textures.minecraft.net/texture/5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1", "§aAdd to lore", "§7Adds to the lore of the item."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.sendTitle("§a§lInput", "§7Enter the line in chat", 5, 80, 5);
                    awaitingInput.put(player.getUniqueId(), input -> {
                        lore.add(input.replace("&", "§"));
                        ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
                        itemCreator.setItem(24, customItem, null);
                        ArrayList<String> tempLore2 = new ArrayList<>();
                        tempLore2.addFirst("§7Use \"&\" for color codes.");
                        tempLore2.add("");
                        tempLore2.addAll(lore);
                        String[] loreArray2 = tempLore2.toArray(new String[tempLore2.size()]);
                        itemCreator.setItem(19, itemCreator.createItem(Material.BOOK, "§eItem Lore", loreArray2), null);
                        openGUI(player, "item_creator");
                    });
                });
                itemCreator.setItem(21, itemCreator.createSkullItemStack(1, "http://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46", "§cRemove last line", "§7Removes the last line of lore."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    if (!lore.isEmpty()) {
                        lore.removeLast();
                        ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    } else {
                        AdminUtilities.sendMessage(player, "The lore is already empty!");
                    }
                });
                itemCreator.setItem(22, itemCreator.createItem(Material.FLINT_AND_STEEL, "§eClear Lore", "§7Clears every line of the lore."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    if (!lore.isEmpty()) {
                        lore.clear();
                        ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
                        itemCreator.setItem(24, customItem, null);
                        openGUI(player, "item_creator");
                    } else {
                        AdminUtilities.sendMessage(player, "The lore is already empty!");
                    }
                });
                itemCreator.setItem(42, itemCreator.createItem(Material.CHEST, "§aGive item", "§7Click to give yourself this item."), (event, _) -> {
                    Player player = (Player) event.getWhoClicked();
                    ItemStack tempcustomItem = updateCustomItem(itemMaterial[0], title[0], lore, glow[0], amount);
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
                    gui.setItem(49, gui.createItem(Material.BARRIER, "§cExit", "§7Click to exit the menu."), (event, _) -> event.getWhoClicked().closeInventory());
                    gui.setItem(48, gui.createItem(Material.ARROW, "§aBack"), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        openGUI(player, "main_menu");
                    });
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

        //NPC Menus
        {
            // NPC Creator
            {
                GUI npcCreator = new GUI("§eNPC Creator", 5);
                npcCreator.setOnCreate((player2, gui) -> {
                    npcCreator.fillInventory(null);
                    //Special Cases
                    if (makingNPC.isVillager()) {
                        npcCreator.setItem(12, npcCreator.createItem(Material.LECTERN, "§eSelect Villager Type", "§7Select the type of villager that the NPC will be."), (event, _) -> {
                            openGUI((Player)event.getWhoClicked(), "villager_type_menu");
                        });
                    }
                    if (makingNPC.isMannequin()) {
                        npcCreator.setItem(12, npcCreator.createItem(Material.ARMOR_STAND, "§eSet Mannequin Skin", "§7Set the skin for the mannequin."), (event, _) -> {
                            openGUI((Player)event.getWhoClicked(), "mannequin_skin_menu");
                        });
                    }
                    npcCreator.setItem(40, npcCreator.createItem(Material.BARRIER, "§cClose"), (event, _) -> {
                        event.getWhoClicked().closeInventory();
                    });
                    npcCreator.setItem(39, npcCreator.createItem(Material.ARROW, "§aBack"), (event, _) -> {
                        openGUI((Player)event.getWhoClicked(), "main_menu");
                    });
                    npcCreator.setItem(25, makingNPC.getNPCEgg(), null);
                    npcCreator.setItem(43, npcCreator.createItem(Material.CHEST, "§aGive NPC Egg", "§7Gives a placeable NPC egg."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.give(makingNPC.getNPCEgg());
                    });
                    npcCreator.setItem(10, npcCreator.createItem(Material.NAME_TAG, "§eNPC Name", "§7Sets the name of the NPC.", "§7Use \"&\" for color codes."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        player.sendTitle("§a§lInput", "§7Enter the name in chat", 5, 80, 5);
                        awaitingInput.put(player.getUniqueId(), input -> {
                            input = input.replace("&", "§");
                            if (makingNPC != null) {
                                makingNPC.setName(input);
                            }
                            npcCreator.setItem(25, makingNPC.getNPCEgg(), null);
                            openGUI(player, "npc_creator");
                        });
                    });
                    npcCreator.setItem(11, npcCreator.createItem(Material.EGG, "§eChange Entity", "§7Change the entity of the NPC.", "", "§7Format:", "§7 - POLAR_BEAR", "§7 - polar bear"),  (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        player.sendTitle("§aInput", "§7Enter the name of the entity in chat.", 5, 80, 5);
                        awaitingInput.put(player.getUniqueId(), input -> {
                            input = input.toUpperCase().replace(" ", "_");
                            EntityType entityType = EntityType.fromName(input);
                            if (entityType != null) {
                                makingNPC.setMobType(entityType);
                            } else {
                                AdminUtilities.sendMessage(player, "Invalid entity name! Valid formats: \"POLAR_BEAR\", \"polar bear\"");
                            }
                            npcCreator.setItem(25, makingNPC.getNPCEgg(), null);
                            openGUI(player, "npc_creator");
                        });
                    });

                    String[] dialogArray = {""};
                    if (makingNPC != null) {
                        dialogArray = makingNPC.getDialog().toArray(new String[makingNPC.getDialog().size()]);
                    }
                    npcCreator.setItem(19, npcCreator.createItem(Material.BOOK, "§eNPC Dialog", dialogArray), null);
                    npcCreator.setItem(20, npcCreator.createSkullItemStack(1, "http://textures.minecraft.net/texture/5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1", "§aAdd To Dialog", "§7Adds a line of dialog.", "§7Use \"&\" for color codes.", ""), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        player.sendTitle("§a§lInput", "§7Enter the line to add in chat.", 5, 80, 5);
                        awaitingInput.put(player.getUniqueId(), input -> {
                            final String line = input.replace("&", "§");
                            Bukkit.getScheduler().runTask(AdminUtilities.getPlugin(), () -> {
                                makingNPC.addDialog(line);

                                ArrayList<String> tempDialog2 = new ArrayList<>();
                                tempDialog2.add("§7Use \"&\" for color codes.");
                                tempDialog2.add("");
                                tempDialog2.addAll(makingNPC.getDialog());
                                String[] dialogArray2 = tempDialog2.toArray(new String[0]);

                                openGUI(player, "npc_creator");

                                npcCreator.setItem(19, npcCreator.createItem(Material.BOOK, "§eNPC Dialog", dialogArray2), null);
                                npcCreator.setItem(25, makingNPC.getNPCEgg(), null);

                            });
                        });
                    });
                    npcCreator.setItem(21, npcCreator.createSkullItemStack(1, "http://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46", "§cRemove Last Line", "§7Removes the last line of dialog."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        if (!makingNPC.getDialog().isEmpty()) {
                            makingNPC.removeDialog();
                            openGUI(player, "npc_creator");
                            npcCreator.setItem(25, makingNPC.getNPCEgg(), null);
                        } else {
                            AdminUtilities.sendMessage(player, "The dialog is already empty!");
                        }
                    });
                    npcCreator.setItem(22, npcCreator.createItem(Material.FLINT_AND_STEEL, "§eClear Dialog", "§7Clears every line of the dialog."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        if (!makingNPC.getDialog().isEmpty()) {
                            makingNPC.clearDialog();
                            npcCreator.setItem(25, makingNPC.getNPCEgg(), null);
                            openGUI(player, "npc_creator");
                        } else {
                            AdminUtilities.sendMessage(player, "The dialog is already empty!");
                        }
                    });
                });
                guis.put("npc_creator", npcCreator);
            }
            // NPC Editor
            {
                GUI npcEditor = new GUI("§eNPC Editor", 5);
                npcEditor.setOnCreate((player2, gui) -> {
                    npcEditor.fillInventory(null);
                    npcEditor.setItem(40, npcEditor.createItem(Material.BARRIER, "§cClose"), (event, _) -> {
                        event.getWhoClicked().closeInventory();
                    });
//                    npcEditor.setItem(25, npcEditor.createItem(Material.VILLAGER_SPAWN_EGG, "§aYour NPC", null), null);
                    npcEditor.setItem(10, npcEditor.createItem(Material.NAME_TAG, "§eNPC Name", "§7Sets the name of the NPC.", "§7Use \"&\" for color codes."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        player.sendTitle("§a§lInput", "§7Enter the name in chat", 5, 80, 5);
                        awaitingInput.put(player.getUniqueId(), input -> {
                            input = input.replace("&", "§");
                            if (editingNPC != null) {
                                editingNPC.setName(input);
                            }
                            openGUI(player, "npc_editor");
                        });
                    });
                    npcEditor.setItem(11, npcEditor.createItem(Material.EGG, "§eChange Entity", "§7Change the entity of the NPC.", "", "§7Format:", "§7 - POLAR_BEAR", "§7 - polar bear"),  (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        player.sendTitle("§aInput", "§7Enter the name of the entity in chat.", 5, 80, 5);
                        awaitingInput.put(player.getUniqueId(), input -> {
                            input = input.toUpperCase().replace(" ", "_");
                            EntityType entityType = EntityType.fromName(input);
                            if (entityType != null) {
                                editingNPC.setMobType(entityType);
                            } else {
                                AdminUtilities.sendMessage(player, "Invalid entity name! Valid formats: \"POLAR_BEAR\", \"polar bear\"");
                            }
                            openGUI(player, "npc_editor");
                        });
                    });

                    ArrayList<String> dialogArray = new ArrayList<>();
                    dialogArray.add("§7Use \"&\" for color codes.");
                    dialogArray.add("");
                    if (editingNPC != null) {
                        dialogArray.addAll(editingNPC.getDialog());
                    }
                    String[] dialogArray1 = dialogArray.toArray(new String[dialogArray.size()]);
                    npcEditor.setItem(19, npcEditor.createItem(Material.BOOK, "§eNPC Dialog", dialogArray1), null);
                    npcEditor.setItem(20, npcEditor.createSkullItemStack(1, "http://textures.minecraft.net/texture/5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1", "§aAdd To Dialog", "§7Adds a line of dialog.", "§7Use \"&\" for color codes."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        player.sendTitle("§a§lInput", "§7Enter the line to add in chat.", 5, 80, 5);
                        awaitingInput.put(player.getUniqueId(), input -> {
                            editingNPC.addDialog(input.replace("&", "§"));
                            ArrayList<String> tempDialog2 = new ArrayList<>();
                            tempDialog2.addFirst("§7Use \"&\" for color codes.");
                            tempDialog2.add("");
                            tempDialog2.addAll(editingNPC.getDialog());
                            String[] dialogArray2 = tempDialog2.toArray(new String[tempDialog2.size()]);
                            openGUI(player, "npc_editor");
                            npcEditor.setItem(19, npcEditor.createItem(Material.BOOK, "§eNPC Dialog", dialogArray2), null);
                        });
                    });
                    npcEditor.setItem(21, npcEditor.createSkullItemStack(1, "http://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46", "§cRemove Last Line", "§7Removes the last line of dialog."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        if (!editingNPC.getDialog().isEmpty()) {
                            editingNPC.removeDialog();
                            openGUI(player, "npc_editor");
                        } else {
                            AdminUtilities.sendMessage(player, "The dialog is already empty!");
                        }
                    });
                    npcEditor.setItem(22, npcEditor.createItem(Material.FLINT_AND_STEEL, "§eClear Dialog", "§7Clears every line of the dialog."), (event, _) -> {
                        Player player = (Player) event.getWhoClicked();
                        if (!editingNPC.getDialog().isEmpty()) {
                            editingNPC.clearDialog();
                            openGUI(player, "npc_editor");
                        } else {
                            AdminUtilities.sendMessage(player, "The dialog is already empty!");
                        }
                    });
                    npcEditor.setItem(28, npcEditor.createItem(Material.DIAMOND_SWORD, "§eRemove NPC", "§7Deletes the NPC."),  (event, _) -> {
                       npcFile.removeNpc(editingNPC);
                       editingNPC = null;
                       event.getWhoClicked().closeInventory();
                    });
                });
                guis.put("npc_editor", npcEditor);
            }
            // Villager Type Selector
            {
                GUI villagerTypeSelector = new GUI("§3Select Villager Type", 5); // 15 professoins
                villagerTypeSelector.fillInventory(null);
                villagerTypeSelector.setItem(40, villagerTypeSelector.createItem(Material.ARROW, "§aBack"),  (event, _) -> {
                    openGUI((Player)event.getWhoClicked(), "npc_creator");
                });
                villagerTypeSelector.setItem(10, villagerTypeSelector.createItem(Material.LECTERN, "§6Librarian"),  (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.LIBRARIAN);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to librarian.");
                });
                villagerTypeSelector.setItem(11, villagerTypeSelector.createItem(Material.BLAST_FURNACE, "§6Armorer"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.ARMORER);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to armorer.");
                });

                villagerTypeSelector.setItem(12, villagerTypeSelector.createItem(Material.SMOKER, "§6Butcher"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.BUTCHER);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to butcher.");
                });

                villagerTypeSelector.setItem(13, villagerTypeSelector.createItem(Material.CARTOGRAPHY_TABLE, "§6Cartographer"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.CARTOGRAPHER);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to cartographer.");
                });

                villagerTypeSelector.setItem(14, villagerTypeSelector.createItem(Material.BREWING_STAND, "§6Cleric"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.CLERIC);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to cleric.");
                });

                villagerTypeSelector.setItem(15, villagerTypeSelector.createItem(Material.COMPOSTER, "§6Farmer"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.FARMER);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to farmer.");
                });

                villagerTypeSelector.setItem(16, villagerTypeSelector.createItem(Material.BARREL, "§6Fisherman"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.FISHERMAN);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to fisherman.");
                });

                villagerTypeSelector.setItem(19, villagerTypeSelector.createItem(Material.FLETCHING_TABLE, "§6Fletcher"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.FLETCHER);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to fletcher.");
                });

                villagerTypeSelector.setItem(20, villagerTypeSelector.createItem(Material.CAULDRON, "§6Leatherworker"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.LEATHERWORKER);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to leatherworker.");
                });

                villagerTypeSelector.setItem(21, villagerTypeSelector.createItem(Material.STONECUTTER, "§6Mason"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.MASON);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to mason.");
                });

                villagerTypeSelector.setItem(22, villagerTypeSelector.createItem(Material.LOOM, "§6Shepherd"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.SHEPHERD);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to shepherd.");
                });

                villagerTypeSelector.setItem(23, villagerTypeSelector.createItem(Material.SMITHING_TABLE, "§6Toolsmith"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.TOOLSMITH);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to toolsmith.");
                });

                villagerTypeSelector.setItem(24, villagerTypeSelector.createItem(Material.GRINDSTONE, "§6Weaponsmith"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.WEAPONSMITH);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to weaponsmith.");
                });

                villagerTypeSelector.setItem(25, villagerTypeSelector.createItem(Material.GREEN_WOOL, "§6Nitwit"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.NITWIT);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to nitwit.");
                });
                villagerTypeSelector.setItem(28, villagerTypeSelector.createItem(Material.BARRIER, "§6None"), (event, _) -> {
                    makingNPC.setVillagerProfession(Villager.Profession.NONE);
                    AdminUtilities.sendMessage((Player)event.getWhoClicked(), "Set NPC profession to none.");
                });
                guis.put("villager_type_selector", villagerTypeSelector);
            }

            // Mannequin skin menu
            {
                GUI mannequinSkinMenu = new GUI("§3Set Mannequin Skin", 1);
                mannequinSkinMenu.fillInventory(null);
                mannequinSkinMenu.setItem(0, mannequinSkinMenu.createItem(Material.ARROW, "§aBack"), (event, gui) -> {
                    openGUI((Player)event.getWhoClicked(), "npc_maker");
                });
                mannequinSkinMenu.setItem(3, mannequinSkinMenu.createItem(Material.COMPASS, "§eSet Skin With URL"), (event, _) -> {
                    //url code
                });
                mannequinSkinMenu.setItem(5, mannequinSkinMenu.createItem(Material.NAME_TAG, "§eSet Skin With Username"), (event, _) -> {
                    //username code
                    Player player = (Player)event.getWhoClicked();
                    player.closeInventory();
                    player.sendTitle("§aInput", "§7Enter the username in chat.", 5, 80, 5);
                    awaitingInput.put(player.getUniqueId(), input ->{
                        makingNPC.setSkin(input, player);
                        openGUI(player, "npc_maker");
                    });
                });
                guis.put("mannequin_skin_menu", mannequinSkinMenu);
            }
        }

        // Settings menu
        {
            GUI settingsMenu = new GUI("§7Plugin Settings", 3);
            settingsMenu.setOnCreate((player2, gui) -> {
                settingsMenu.fillEdge(null);
                settingsMenu.setItem(22, settingsMenu.createItem(Material.BARRIER, "§cClose", null), (event, _) -> {
                    event.getWhoClicked().closeInventory();
                });
                settingsMenu.setItem(21, settingsMenu.createItem(Material.ARROW, "§aBack", null), (event, _) -> {
                    openGUI((Player)event.getWhoClicked(), "main_menu");
                });
                settingsMenu.setItem(10, settingsMenu.createItem(Material.BLUE_ICE, "§bFreeze §7Settings", "§7Settings for the freeze menu."), (event, _) -> {
                    openGUI((Player)event.getWhoClicked(), "freeze_settings");
                });
            });
            guis.put("settings_menu", settingsMenu);
        }
        // Freeze Settings
        {
            GUI freezeSettings = new GUI("§bFreeze §7Settings", 3);
            freezeSettings.setOnCreate((player2, gui) -> {
                freezeSettings.fillInventory(null);
                freezeSettings.setItem(22, freezeSettings.createItem(Material.BARRIER, "§cClose", null), (event, _) -> {
                    event.getWhoClicked().closeInventory();
                });
                freezeSettings.setItem(21, freezeSettings.createItem(Material.ARROW, "§aBack", null), (event, _) -> {
                    openGUI((Player)event.getWhoClicked(), "settings_menu");
                });
                String setting1 = (AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-interact")) ? "§aALLOWED" : "§cBLOCKED";
                freezeSettings.setItem(10, freezeSettings.createItem(Material.GRASS_BLOCK, "§eAllow Block Interactions", "§7This setting allows the frozen player to", "§7interact with blocks.", "", "§7This includes breaking, placing, using, etc.", "", "§7Status: " + setting1), (event, _) -> {
                    boolean settingValue = AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-interact");
                    AdminUtilities.getPlugin().getConfig().set("freeze-settings.allow-interact", !settingValue);
                    openGUI((Player)event.getWhoClicked(), "freeze_settings");
                });
                String setting2 = (AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-chat")) ? "§aALLOWED" : "§cBLOCKED";
                freezeSettings.setItem(11, freezeSettings.createItem(Material.OAK_SIGN, "§eAllow Player Chat", "§7This setting allows the frozen","§7player to send chat messages.", "", "§7Status: " + setting2), (event, _) -> {
                    boolean settingValue = AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-chat");
                    AdminUtilities.getPlugin().getConfig().set("freeze-settings.allow-chat", !settingValue);
                    openGUI((Player)event.getWhoClicked(), "freeze_settings");
                });
                String setting3 = (AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.teleport-player-to-ground")) ? "§aENABLED" : "§cDISABLED";
                freezeSettings.setItem(12, freezeSettings.createItem(Material.ENDER_PEARL, "§eTeleport Frozen Player", "§7This setting teleports the frozen","§7player to the highest block at their position.", "", "§7Status: " + setting3), (event, _) -> {
                    boolean settingValue = AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.teleport-player-to-ground");
                    AdminUtilities.getPlugin().getConfig().set("freeze-settings.teleport-player-to-ground", !settingValue);
                    openGUI((Player)event.getWhoClicked(), "freeze_settings");
                });
                String setting4 = (AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-camera-movement")) ? "§aALLOWED" : "§cBLOCKED";
                freezeSettings.setItem(13, freezeSettings.createItem(Material.SPYGLASS, "§eAllow Player Camera Movement", "§7This setting allows the frozen","§7player to move their camera while frozen.", "", "§7Status: " + setting4), (event, _) -> {
                    boolean settingValue = AdminUtilities.getPlugin().getConfig().getBoolean("freeze-settings.allow-camera-movement");
                    AdminUtilities.getPlugin().getConfig().set("freeze-settings.allow-camera-movement", !settingValue);
                    openGUI((Player)event.getWhoClicked(), "freeze_settings");
                });
            });
            guis.put("freeze_settings", freezeSettings);
        }
    }

    public static GUI openGUI(Player player, String key) {
        GUI gui = getGUI(key);
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
        return gui;
    }
    public static void openNPCEditor(Player player, npc npc) {
        GUI gui = getGUI("npc_editor");
        editingNPC = npc;
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
    }

    private static ItemStack updateCustomItem (Material material, String title, ArrayList<String> lore, boolean glow, int amount) {
        ItemStack newItem = new ItemStack(material, amount);
        ItemMeta meta = newItem.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(lore);
        meta.setEnchantmentGlintOverride(glow);
        newItem.setItemMeta(meta);
        return newItem;
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
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openGUIs.remove(event.getPlayer().getUniqueId());
    }
}
