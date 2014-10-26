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

public class PortalUnstuck extends JavaPlugin implements Listener {

	private static final boolean isDebugging = true;

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){

		if(cmd.getName().equalsIgnoreCase("unstuck")){ // If the player typed /unstuck then do the following...
			if (args.length < 1) {
				sender.sendMessage("Which player to unstuck???");
				return false;
			}

			Player target = (Bukkit.getServer().getPlayer(args[0]));
			if (target == null) {
				sender.sendMessage(args[0] + " is not online!");
				return false;
			}
			
			performUnstuck(PortalUnstuck.this, target, sender);
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
		performUnstuck(plugin, player, null);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
//		getLogger().info(event.getPlayer().getName() + " joined the server! :D");
//		JavaPlugin plugin = PortalUnstuck.this;
//		
//		final Player player = event.getPlayer();
//		performUnstuck(plugin, player, null);
	}
	
	
	private void performUnstuck(JavaPlugin plugin, final Player player, final CommandSender sender) {

		final int directions[][] = {
				{1, 0, 0},
				{-1, 0, 0},
				{0, 0, 1},
				{0, 0, -1},
		};
		
		final Location location = player.getLocation();
		
		if(isDebugging){
			for (int[] dir : directions) {
				System.out.println("location=" + location);
				Block relative = location.getBlock().getRelative(dir[0], dir[1], dir[2]);
				getLogger().info("At " + relative.getLocation() + " there is "+ relative.getType());
			}
		}
		
		
		Material type = location.getBlock().getType();
		getLogger().info("Right now " + player.getName() + " is in "+type.toString());
		if (type == Material.PORTAL) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					
					check: for (int[] dir : directions) {
						Block relative = location.getBlock().getRelative(dir[0], dir[1], dir[2]);
						
						Block blockFloor = relative.getRelative(0, -1, 0);
						if (blockFloor.isEmpty()) {
							continue;
						} else {
							Block blockAbove = relative.getRelative(0, 2, 0);
							if (blockAbove.isEmpty() ) {
								player.teleport(relative.getLocation());
								
								String resquer = "PortalUnstuck";
								if (sender != null) {
									resquer = sender.getName();
								}
								
								player.sendMessage(resquer  + " detected you were stuck in portal and moved you a bit!");
								if (sender != null) {
									sender.sendMessage("Thanks, you saved "+player.getName());
								}
								getLogger().info("Unstucking player " + player.getName() + " to " + relative.getLocation());
								break check;
							}
						}
					}//foreach direction
				}
			}, 20L);
		}
	}//eof performUnstuck


}
