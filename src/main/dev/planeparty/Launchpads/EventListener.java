package dev.planeparty.Launchpads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

import net.wesjd.anvilgui.AnvilGUI;
import net.wesjd.anvilgui.AnvilGUI.Builder;


public class EventListener implements Listener {
	
	static List<Material> blocks;
	
	static List<Player> fallimmunity;
	
	public EventListener (Main plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		Material[] blockarray = {Material.STONE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.JUNGLE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.MANGROVE_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE, Material.POLISHED_BLACKSTONE_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE};
		blocks = new ArrayList<>();
		for (Material m : blockarray) {
			blocks.add(m);
		}
		fallimmunity = new ArrayList<>();
	}
    
    @EventHandler
    public void onFall(EntityDamageEvent e) {
    	if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL && fallimmunity.contains((Player) e.getEntity())) {
    		e.setCancelled(true);
    		fallimmunity.remove((Player) e.getEntity());
    	}
    }
    
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
    	if (Main.launchinfo.containsKey(e.getPlayer())) {
    		ItemStack i = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
			ItemMeta meta = i.getItemMeta();
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.setDisplayName(ChatColor.GOLD + "Launch Direction Selector");
			i.setItemMeta(meta);
    		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem().equals(i)) {
    			Launchpad pad = (Launchpad) Main.launchinfo.get(e.getPlayer()).get("launchpad");
    			Vector v = e.getPlayer().getFacing().getDirection();
    			v.multiply(pad.strength);
    			pad.direction = v;
    			e.getPlayer().sendMessage(ChatColor.GOLD + "Set launch direction");
    			Main.launchpads.add(pad);
    			Main.saveLaunchpads();
    			e.getPlayer().getInventory().setItemInMainHand((ItemStack) Main.launchinfo.get(e.getPlayer()).get("item"));
    			Main.launchinfo.remove(e.getPlayer());
    		}
    	} else if (e.getAction() == Action.PHYSICAL && blocks.contains(e.getClickedBlock().getType())) {
    		for (Launchpad pad : Main.launchpads) {
    			if (pad.location.equals(e.getClickedBlock().getLocation())) {
    				Vector v = e.getPlayer().getVelocity().add(pad.direction).setY(pad.vertical);
    				e.getPlayer().setVelocity(v);
    				fallimmunity.add(e.getPlayer());
    				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f);
    				Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
    					public void run() {
    						if (fallimmunity.contains(e.getPlayer())) {
    							fallimmunity.remove(e.getPlayer());
    						}
    					}
    				}, 100L);
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
    	if (e.isCancelled()) { return; }
    	if (blocks.contains(e.getBlock().getType()) && Main.padplace.contains(e.getPlayer())) {
    		Launchpad pad = new Launchpad(e);
    		Builder vertical = new AnvilGUI.Builder();
    		Builder power = new AnvilGUI.Builder();
        	vertical.interactableSlots();
        	power.interactableSlots();
        	vertical.title("Enter Vertical Launch");
        	power.title("Enter Launch Power");
        	vertical.itemOutput(new ItemStack(Material.PAPER));
        	power.itemOutput(new ItemStack(Material.PAPER));
        	vertical.itemLeft(new ItemStack(Material.PAPER));
        	power.itemLeft(new ItemStack(Material.PAPER));
        	vertical.plugin((Plugin) Main.getInstance());
        	power.plugin((Plugin) Main.getInstance());
        	vertical.onClick((slot, stateSnapshot) -> {
        		if (slot != AnvilGUI.Slot.OUTPUT) {
        			return Collections.emptyList();
        		}
        		if (!(Double.parseDouble(stateSnapshot.getText()) > 0)) {
        			return Collections.emptyList();
        		}
        		pad.vertical = Double.parseDouble(stateSnapshot.getText());
        		power.open(e.getPlayer());
        		return Arrays.asList(AnvilGUI.ResponseAction.close());
        	});
        	power.onClick((slot, stateSnapshot) -> {
        		if (slot != AnvilGUI.Slot.OUTPUT) {
        			return Collections.emptyList();
        		}
        		if (!(Double.parseDouble(stateSnapshot.getText()) > 0)) {
        			return Collections.emptyList();
        		}
        		pad.strength = Double.parseDouble(stateSnapshot.getText());
        		return Arrays.asList(AnvilGUI.ResponseAction.close());
        	});
        	vertical.open(e.getPlayer());
    		power.onClose(stateSnapshot -> {
    			HashMap<String, Object> minimap = new HashMap<>();
    			minimap.put("stage", "direction");
    			minimap.put("item", e.getPlayer().getInventory().getItemInMainHand());
    			minimap.put("launchpad", pad);
    			Main.launchinfo.put(e.getPlayer(), minimap);
    			ItemStack i = new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
    			ItemMeta meta = i.getItemMeta();
    			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
    			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    			meta.setDisplayName(ChatColor.GOLD + "Launch Direction Selector");
    			i.setItemMeta(meta);
    			e.getPlayer().getInventory().setItemInMainHand(i);
    			e.getPlayer().sendMessage(ChatColor.GOLD + "Look in the direction you want the launchpad to launch, then click with the selector item");
    			Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
    				public void run() {
    					if (Main.launchinfo.containsKey(e.getPlayer())) {
	    					e.getPlayer().getInventory().setItemInMainHand((ItemStack) Main.launchinfo.get(e.getPlayer()).get("item"));
	    					pad.location.getBlock().setType(Material.AIR);
	    					e.getPlayer().sendMessage(ChatColor.RED + "Launch Pad creation cancelled");
	    				}
    				}
    			}, 600L);
    		});
    	}
    }
    
    @EventHandler
    public void onSwitch(PlayerItemHeldEvent e) {
    	if (Main.launchinfo.containsKey(e.getPlayer())) {
    		e.getPlayer().getInventory().setHeldItemSlot(e.getPreviousSlot());
    	}
    }
    
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
    	if (blocks.contains(e.getBlock().getType())) {
    		for (Launchpad pad : Main.launchpads) {
        		if (pad.location.equals(e.getBlock().getLocation())) {
        			e.getPlayer().sendMessage(ChatColor.GOLD + "Removed Launchpad");
        			Main.launchpads.remove(pad);
        			Main.saveLaunchpads();
        		}
        	}
    	} else { 
    		Location l = new Location(e.getBlock().getWorld(), e.getBlock().getX(), e.getBlock().getLocation().getY() + 1, e.getBlock().getZ());
    		if (blocks.contains(e.getBlock().getWorld().getBlockAt(l).getType())) {
    			for (Launchpad pad : Main.launchpads) {
            		if (pad.location.equals(e.getBlock().getLocation())) {
            			e.getPlayer().sendMessage(ChatColor.GOLD + "Removed Launchpad");
            			Main.launchpads.remove(pad);
            			Main.saveLaunchpads();
            		}
            	}
    		}
    	}
    }
    
}