package dev.planeparty.Launchpads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

public class Launchpad {
	
	public Launchpad(BlockPlaceEvent e) {
		material = e.getBlock().getType();
		location = e.getBlock().getLocation();
	}
	
	public Launchpad() {}
	
	Material material;
	
	Location location;
	
	double vertical;
	
	double strength;
	
	Vector direction;
	
}
