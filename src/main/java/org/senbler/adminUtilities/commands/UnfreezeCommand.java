package org.senbler.adminUtilities.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.senbler.adminUtilities.AdminUtilities;

import java.util.ArrayList;
import java.util.List;

import static org.senbler.adminUtilities.commands.FreezeCommand.isFrozen;
import static org.senbler.adminUtilities.commands.FreezeCommand.setFrozen;

public class UnfreezeCommand implements CommandExecutor {
    public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {

        List<Player> playersToUnfreeze = new ArrayList<>();
        if (args.length == 0) {
            AdminUtilities.sendMessage((Player)sender, "You must enter the name of a player or players to §bunfreeze§7.");
            return false;
        }
        for (String s : args) {
            Player p = Bukkit.getPlayer(s);
            if (p == null) {
                AdminUtilities.sendMessage((Player)sender, "Player \"" + s + "\" does not exist.");
            }
            else if (!isFrozen(p)){
                AdminUtilities.sendMessage((Player)sender, "Player \"" + p.getName() + "\" is not §bfrozen.");
            } else {
                playersToUnfreeze.add(p);
            }
        }
        for (Player p : playersToUnfreeze) {
            setFrozen(p, false);
            p.sendMessage("§7You have been §bunfrozen§7.");
            AdminUtilities.sendMessage((Player)sender, p.getName() + " has been §bunfrozen§7.");
        }
        return true;
    }
}
