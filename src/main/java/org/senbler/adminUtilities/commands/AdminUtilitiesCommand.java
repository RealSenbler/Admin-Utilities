package org.senbler.adminUtilities.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.senbler.adminUtilities.menus.Menus;

public class AdminUtilitiesCommand implements CommandExecutor {

    /*private GUI gui;
    public test(GUI gui) {
        this.gui = gui;
    }*/

    @Override
    public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (player == null) {
            return false;
        }
        Menus.openGUI(player, "main_menu");
        //gui.open(player);
        return true;
    }
}
