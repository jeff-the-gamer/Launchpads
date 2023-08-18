package dev.planeparty.Launchpads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;


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
    public void onInteract(PlayerInteractEvent e) {
    	if (e.getAction() == Action.PHYSICAL && blocks.contains(e.getClickedBlock().getType())) {
    		for (Launchpad pad : Main.launchpads) {
    			if (pad.location == e.getClickedBlock().getLocation()) {
    				e.getPlayer().getVelocity().add(pad.direction).setY(pad.vertical);
    				fallimmunity.add(e.getPlayer());
    				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f);
    				Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
    					public void run() {
    						fallimmunity.remove(e.getPlayer());
    					}
    				}, 100L);
    			}
    		}
    	}
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
    		if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getItem() == i) {
    			Launchpad pad = (Launchpad) Main.launchinfo.get(e.getPlayer()).get("launchpad");
    			Vector v = e.getPlayer().getFacing().getDirection();
    			v.multiply(pad.strength);
    			pad.direction = v;
    			e.getPlayer().sendMessage(ChatColor.GOLD + "Set launch direction");
    			Main.launchpads.add(pad);
    			Main.saveLaunchpads();
    			e.getPlayer().getInventory().setItemInMainHand((ItemStack) Main.launchinfo.get(e.getPlayer()).get("item"));
    		}
    	}
    }
    
    @EventHandler
    public void onSignClose(SignChangeEvent e) {
    	if (e.isCancelled()) { return; }
    	if (Main.launchinfo.containsKey(e.getPlayer())) {
    		if (Main.launchinfo.get(e.getPlayer()).get("state").toString().equalsIgnoreCase("vertical")) {
    			((Launchpad) Main.launchinfo.get(e.getPlayer()).get("launchpad")).vertical = Double.parseDouble(e.getLine(0));
    			Block sb = e.getBlock().getLocation().getBlock();
        		sb.setType(Material.OAK_SIGN);
        		Sign sign = (Sign) sb;
        		sb.setType(((Launchpad) Main.launchinfo.get(e.getPlayer()).get("launchpad")).material);
        		sign.setLine(0, "");
        		sign.setLine(1, "--------------");
        		sign.setLine(2, "Please enter");
        		sign.setLine(3, "Launch Strength");
        		e.getPlayer().openSign(sign);
        		HashMap<String, Object> minimap = Main.launchinfo.get(e.getPlayer());
        		minimap.put("stage", "velocity");
        		Main.launchinfo.put(e.getPlayer(), minimap);
    		} else if (Main.launchinfo.get(e.getPlayer()).get("state").toString().equalsIgnoreCase("velocity")) {
    			((Launchpad) Main.launchinfo.get(e.getPlayer()).get("launchpad")).strength = Double.parseDouble(e.getLine(0));
    			HashMap<String, Object> minimap = Main.launchinfo.get(e.getPlayer());
    			minimap.put("stage", "direction");
    			minimap.put("item", e.getPlayer().getInventory().getItemInMainHand());
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
    					e.getPlayer().getInventory().setItemInMainHand((ItemStack) Main.launchinfo.get(e.getPlayer()).get("item"));
    					Launchpad pad = (Launchpad) Main.launchinfo.get(e.getPlayer()).get("launchpad");
    					pad.location.getBlock().setType(Material.AIR);
    					e.getPlayer().sendMessage(ChatColor.RED + "Launch Pad creation cancelled");
    				}
    			}, 600L);
    		}
    	}
    }
    
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
    	if (e.isCancelled()) { return; }
    	if (blocks.contains(e.getBlock().getType()) && Main.padplace.contains(e.getPlayer())) {
    		Launchpad pad = new Launchpad(e);
    		Block sb = e.getBlock().getLocation().getBlock();
    		sb.setType(Material.OAK_SIGN);
    		Sign sign = (Sign) sb;
    		sb.setType(pad.material);
    		sign.setLine(0, "");
    		sign.setLine(1, "--------------");
    		sign.setLine(2, "Please enter");
    		sign.setLine(3, "Vertical Launch");
    		e.getPlayer().openSign(sign);
    		HashMap<String, Object> minimap = new HashMap<>();
    		minimap.put("stage", "vertical");
    		minimap.put("launchpad", pad);
    		Main.launchinfo.put(e.getPlayer(), minimap);
    	}
    }
}