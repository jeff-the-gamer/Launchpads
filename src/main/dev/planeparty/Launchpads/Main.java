package dev.planeparty.Launchpads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * Launchpads
 * @author Jeff Washburn
 * @version 1.0.0
 *
 */
public class Main extends JavaPlugin {
	public static List<Player> padplace = new ArrayList<>();
	public static List<Launchpad> launchpads = new ArrayList<>();
	public static HashMap<Player, HashMap<String, Object>> launchinfo = new HashMap<>();
    private static Main plugin;
    @Override
    public void onEnable() {
        plugin = this;
        new EventListener(this);
        this.getCommand("launchpad").setExecutor(new CommandLaunchpad());
        File padFile = new File(Main.getInstance().getDataFolder() + "launchpads.yml");
        if (!padFile.exists()) {
            try {
                padFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration padFileYaml = new YamlConfiguration();
        try {
            padFileYaml.load(padFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        for (Object o : padFileYaml.getKeys(false)) {
        	Launchpad pad = new Launchpad();
        	pad.location = padFileYaml.getLocation(o + ".location");
        	pad.material = Material.valueOf(padFileYaml.getString(o + ".material"));
        	pad.vertical = padFileYaml.getDouble(o + ".vertical");
        	pad.direction = padFileYaml.getVector(o + ".direction");
        	launchpads.add(pad);
        }
        
    }
    @Override
    public void onDisable() {
    }
    public static Main getInstance(){
        return plugin;
    }
    public static void saveLaunchpads() {
    	File padFile = new File(Main.getInstance().getDataFolder() + "launchpads.yml");
    	FileConfiguration padFileYaml = new YamlConfiguration();
        try {
            padFileYaml.load(padFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        int i = 0;
    	for (Launchpad pad : launchpads) {
    		padFileYaml.set(i + ".location", pad.location);
    		padFileYaml.set(i + ".material", pad.material);
    		padFileYaml.set(i + ".vertical", pad.vertical);
    		padFileYaml.set(i + ".direction", pad.direction);
    		i++;
    	}
    	try {
			padFileYaml.save(padFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
