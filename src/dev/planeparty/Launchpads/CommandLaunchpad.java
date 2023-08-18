package dev.planeparty.Launchpads;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandLaunchpad implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
        	sender.sendMessage(ChatColor.RED + "This is a Player-Only command");
        } else if (!((Player) sender).hasPermission("launchpads.place")) {
        	sender.sendMessage(ChatColor.RED + "Invalid permissions");
        } else {
        	Player p = (Player) sender;
        	if (args.length == 0) {
        		if (Main.padplace.contains(p)) {
        			Main.padplace.remove(p);
        			p.sendMessage(ChatColor.GOLD + "Launchpad placing toggled " + ChatColor.RED + "OFF");
        		} else {
        			Main.padplace.add(p);
        			p.sendMessage(ChatColor.GOLD + "Launchpad placing toggled " + ChatColor.GREEN + "ON");
        		}
        	} else if (args[0].equalsIgnoreCase("on")) {
        		if (!Main.padplace.contains(p)) {
        			Main.padplace.add(p);
        		}
        		p.sendMessage(ChatColor.GOLD + "Launchpad placing toggled " + ChatColor.GREEN + "ON");
        	} else if (args[0].equalsIgnoreCase("off")) {
        		if (Main.padplace.contains(p)) {
        			Main.padplace.remove(p);
        		}
        		p.sendMessage(ChatColor.GOLD + "Launchpad placing toggled " + ChatColor.RED + "OFF");
        	} else {
        		if (Main.padplace.contains(p)) {
        			Main.padplace.remove(p);
        			p.sendMessage(ChatColor.GOLD + "Launchpad placing toggled " + ChatColor.RED + "OFF");
        		} else {
        			Main.padplace.add(p);
        			p.sendMessage(ChatColor.GOLD + "Launchpad placing toggled " + ChatColor.GREEN + "ON");
        		}
        	}
         }
    	return true;
    }
}