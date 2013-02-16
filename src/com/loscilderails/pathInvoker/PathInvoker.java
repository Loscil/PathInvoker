package com.loscilderails.pathInvoker;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;

/*
 * @author Loscil Derails
 * @version 1.0
 * Allows for the creation of paths by running over blocks.
 */

public class PathInvoker extends JavaPlugin implements Listener, CommandExecutor{ 
        
		public void onEnable(){
			this.saveDefaultConfig();
			getLogger().info("PathInvoker has been enabled :>");
	        getServer().getPluginManager().registerEvents(this, this);
	        this.getCommand("path").setExecutor(this);
	        getServer().getPluginManager().registerEvents(this, this);
		}
		
		public void onDisable(){
			getLogger().info("PathInvoker has been disabled :<");
		}

		// Enables the use of the /path reload command
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		    final Player player = (Player) sender;
		    if(player.isOp() && cmd.getName().equalsIgnoreCase("path")){
		    	if (args.length <=0) sender.sendMessage("Arguments: reload");
		    	else{
			    	if(args[0].equals("reload")){
			    		this.reloadConfig();
			    		sender.sendMessage("Reloaded");
			    		return true;
			    	}
			    	else{
			    		sender.sendMessage("Invalid Command");
			    	}
		    	}
		    } else if ((cmd.getName().equalsIgnoreCase("checkpoint"))){
		    	sender.sendMessage("Op only command");
		    	return true;
		    }
		    return false;	
		}
		
		@EventHandler
		public void onPlayerMove(PlayerMoveEvent evt) {
			// Load the number of block replacements to check for
			int numReplacements = PathInvoker.this.getConfig().getInt("numReplacements");
			// Get player location, and get the block they're standing on
			Location loc = evt.getPlayer().getLocation();
			World w = loc.getWorld();
			loc.setY(loc.getY() - 1);
			Block b = w.getBlockAt(loc);
			// If we're on a different block, do replacements. Do nothing if we're on the same block
			if(!(evt.getFrom().getBlockX() == evt.getTo().getBlockX() && evt.getFrom().getBlockZ() == evt.getTo().getBlockZ())){
				evt.setFrom(evt.getTo());
				// To tell if a block has been replaced once
				boolean blockReplaced = false;
				// To tell if we've already checked for growing grass (one block above where player is standing)
				// Only want one block replacement per block change at maximum.
				boolean blockChecked = false;
				if(PathInvoker.this.getConfig().getBoolean("trampleGrass")){
					loc.setY(loc.getY() + 1);
					Block grass = w.getBlockAt(loc);
					// if its grass
					if(grass.getTypeId()==31){
						blockChecked = true;
						// If we meet the probability requirement
						if(Math.random()*PathInvoker.this.getConfig().getInt("probabilityValue") < PathInvoker.this.getConfig().getInt("grassTrampleProbability")){
							// Set it to air
							grass.setTypeId(0);	
							blockReplaced = true;
						}
					}
				}
				// If we haven't already checked tall grass, look through the rest
				if(!blockChecked){
					// Get block ID that we're standing on
					int blockID = b.getTypeId();
					// Look through all of the replacement configs, each called "replace<integer>"
					// 'i' will range from 1 to the number of block replacements declared
					for(int i = 1; i < numReplacements+1; i++){
						// If we haven't already replaced a block and the block ID matches the config replace from...
						if(!blockReplaced && blockID == PathInvoker.this.getConfig().getInt("replace" + i + ".from")){
							// ...see if we match the probability requirement
							if(Math.random()*PathInvoker.this.getConfig().getInt("probabilityValue") < PathInvoker.this.getConfig().getInt("replace" + i + ".probability")){
								// Replace it to the block to change it to
								b.setTypeId(PathInvoker.this.getConfig().getInt("replace" + i + ".to"));
								blockReplaced = true;
							}
						}
					}
				}
			}
		}
}
