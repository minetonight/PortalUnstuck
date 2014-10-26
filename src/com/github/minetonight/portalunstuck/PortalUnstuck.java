package com.github.minetonight.portalunstuck;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class PortalUnstuck extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("unstuck")){ // If the player typed /unstuck then do the following...
			// doSomething
			if (args.length < 1) {
				sender.sendMessage("Which player to unstuck???");
				return false;
			}

			Player target = (Bukkit.getServer().getPlayer(args[0]));
			if (target == null) {
				sender.sendMessage(args[0] + " is not online!");
				return false;
			}
			
			performUnstuck(PortalUnstuck.this, target);
			getLogger().info(sender.getName() + " performed command " + cmd.getName());
			
			return true;
		} //If this has happened the function will return true. 

		// If this hasn't happened the a value of false will be returned.
		return false; 
	}

	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		getLogger().info(event.getPlayer().getName() + " logged in the server! :D");
		JavaPlugin plugin = PortalUnstuck.this;

		final Player player = event.getPlayer();
		performUnstuck(plugin, player);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		getLogger().info(event.getPlayer().getName() + " joined the server! :D");
		JavaPlugin plugin = PortalUnstuck.this;
		
		final Player player = event.getPlayer();
		performUnstuck(plugin, player);
	}
	
	private void performUnstuck(JavaPlugin plugin, final Player player) {
		final Location location = player.getLocation();

		Vector directions[] = {
				new Vector(-1, 0, 0),
				new Vector(0, 0, -1),
		};
		
		
		for (Vector direction : directions) {
			System.out.println("direction=" + direction);
			Location newLocation = location.add(direction);
			getLogger().info("At "+newLocation.toString()+ " there is "+ newLocation.getBlock().getType());
			newLocation = location.subtract(direction);
			getLogger().info("At "+newLocation.toString()+ " there is "+ newLocation.getBlock().getType());
		}
		
		
		
		Material type = location.getBlock().getType();
		getLogger().info("Right now " + player.getName() + " is in "+type.toString());
		if (type == Material.PORTAL) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					Vector directions[] = {
							new Vector(1, 0, 0),
							new Vector(-1, 0, 0),
							new Vector(0, 0, 1),
							new Vector(0, 0, -1),
							};
					
					for (Vector direction : directions) {
						Location newLocation = location.add(direction);
						
						Block blockBelow = newLocation.add(0, -1, 0).getBlock();
						if (blockBelow.isEmpty()) {
							continue;
						} else {
							Block blockAbove = newLocation.add(0, 2, 0).getBlock();
							if (blockAbove.isEmpty() ) {
								player.teleport(newLocation);
								player.sendMessage("We detected you were stuck in portal and moved you a bit!");
								getLogger().info("Unstucking player " + player.getName() + " to " + newLocation.toString());
								break;
							}
						}
					}//foreach direction
				}
			}, 2L);
		}
	}//eof performUnstuck


}
