package org.senbler.adminUtilities.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.senbler.adminUtilities.npc.npc;
import org.senbler.adminUtilities.npc.npcFile;

public class spawnNPC implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player)sender;
        npc npc = new npc("§eTEST NPC", EntityType.SKELETON);
        npc.addDialog("Hello");
        npc.addDialog("World");
        npc.spawnNPC(player);
        npcFile.addNpc(npc);

        return true;
    }
}
