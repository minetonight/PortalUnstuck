package com.github.minetonight.portalunstuck;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
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
			
			checkStuck(PortalUnstuck.this, target, sender);
			getLogger().info(sender.getName() + " performed command " + cmd.getName());
			
			return true;
		} //If this has happened the function will return true. 

		// If this hasn't happened the a value of false will be returned.
		return false; 
	}

	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (isDebugging) {
			getLogger().info(event.getPlayer().getName() + " logged in the server!");
		}
		//too early called?
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (isDebugging) {
			getLogger().info(event.getPlayer().getName() + " joined the server!");
		}
		JavaPlugin plugin = PortalUnstuck.this;
		
		final Player player = event.getPlayer();
		checkStuck(plugin, player, null);
	}
	
	
	/**
	 * Intends to prevent portal stuck on teleport level.
	 * TODO still not updating the event#to Location
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPortal(PlayerPortalEvent event) {
		
		TeleportCause cause = event.getCause();
		Player player = event.getPlayer();
		Location from = event.getFrom();
		Location to = event.getTo();

		if (isDebugging) {
			
			System.out.println("player=" + player);
			System.out.println("from=" + from);
			System.out.println("to=" + to);
			System.out.println("cause=" + cause);
		}
		
//		if (cause == TeleportCause.NETHER_PORTAL) {
//			event.setTo(event.getTo().add(0.5, 0, 0.5));
//		}
	}//eof onPlayerTeleport
	
	
	private void checkStuck(JavaPlugin plugin, final Player player, final CommandSender sender) {
		
		if (isNextToPortal(player.getLocation())
				&& isAtRoundCoordinates(plugin, player, sender)) {
			
			performUnstuck(plugin, player, sender);
		}

		return;
	}//eof checkStuck
	
	
	private boolean isAtRoundCoordinates(JavaPlugin plugin, final Player player, final CommandSender sender) {
		
		Location location = player.getLocation();
		
		double dx = location.getX() - Math.floor(location.getX());
		double dz = location.getZ() - Math.floor(location.getZ());
		System.out.println("dx=" + dx);
		System.out.println("dz=" + dz);
		
		boolean flag = false;
		if (dx < 0.01 && dz < 0.01) { // overworld
			flag = true;
		}
		else if (dx > 0.49 && dx < 0.51 && dz < 0.01) { // nether
			flag = true;
		}
		else if (dx < 0.01 && dz > 0.49 && dz < 0.51) {
			flag = true;
		}
		else if (dx > 0.49 && dx < 0.51 && dz > 0.49 && dz < 0.51) {
			flag = true;
		}
		
		if (flag) {
			getLogger().info("It looks like you are just teleported.");
		}
		return flag;
	}//eof isAtRoundCoordinates

	
	final int directionsFour[][] = {
			{1, 0, 0},
			{-1, 0, 0},
			{0, 0, 1},
			{0, 0, -1},
	};
	
	
	private boolean isNextToPortal(Location location) {
		
		Block block = location.getBlock();
		Material type;

		boolean flag = false;
		
		//check block at location
		type = block.getType();
		if(isDebugging){
			getLogger().info("The location is in "+type.toString());
		}
		if (type == Material.PORTAL) {
			flag = true;
		}

		if ( ! flag) {
			//check blocks around
			for (int[] dir : directionsFour) {
				Block relative = block.getRelative(dir[0], dir[1], dir[2]);
				
				if(isDebugging){
					System.out.println("location=" + location);
					getLogger().info("At " + relative.getLocation() + " there is "+ relative.getType());
				}
				
				type = relative.getType();
				if (type == Material.PORTAL) {
					flag = true;
				}
			}
		}
		
		if (flag) {
			getLogger().info("It looks like you are next to portal.");
		}
		return flag;
	}//eof isNextToPortal
	
	
	final int directionsAroundPortal[][] = {
			{-2, 0, 0},
			{-1, 0, 0},
			{1, 0, 0},
			{2, 0, 0},
			{0, 0, -2},
			{0, 0, -1},
			{0, 0, 1},
			{0, 0, 2},
			
			{1, 0, -1},
			{1, 0, 1},
			{-1, 0, 1},
			{-1, 0, -1},
			
	};
	private void performUnstuck(JavaPlugin plugin, final Player player, final CommandSender sender) {

		final Location location = player.getLocation();
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {

				check: for (int[] dir : directionsAroundPortal) {
					System.out.println("dir=" + dir);
					Block relative = location.getBlock().getRelative(dir[0], dir[1], dir[2]);

					Block blockFloor = relative.getRelative(0, -1, 0);
					if (blockFloor.isEmpty() || blockFloor.isLiquid()) {
						System.out.println("Is that last iteration????");
						continue; // no floor, keep searching
					} else {
						Block blockAbove = relative.getRelative(0, 1, 0);
						if (blockAbove.isEmpty() && 
								! isNextToPortal(relative.getLocation())) {
							
							System.out.println("We found safe spot");
							player.teleport(relative.getLocation().add(0.5, 0, 0.5));

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
						else {
							System.out.println("Bad option at +" + relative.getLocation());
						}
					}
				}//foreach direction
			}
		}, 20L);
	}//eof performUnstuck


}
