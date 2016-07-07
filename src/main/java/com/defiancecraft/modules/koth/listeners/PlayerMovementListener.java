package com.defiancecraft.modules.koth.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.defiancecraft.modules.koth.config.components.SerialSelection;
import com.defiancecraft.modules.koth.game.GameManager;

/**
 * A listener for the movement of players, especially in/out of the
 * region defined for KOTH. Note that 'movement' is defined as simply
 * moving across blocks, disconnecting, changing worlds, etc.
 */
public class PlayerMovementListener implements Listener {

	private boolean informedOfError = false;
	private GameManager man;
	
	public PlayerMovementListener(GameManager man) {
		this.man = man;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// Ignore if game is not running
		if (!man.isGameRunning())
			return;

		// Ignore if not moving across blocks
		if (e.getTo().getBlock().equals(e.getFrom().getBlock()))
			return;
		
		SerialSelection region = man.getPlugin().getConfiguration().region;
		
		// Ignore if region is undefined in config
		if (region == null) {
			handleNullRegion();
			return;
		}
		
		boolean fromInRegion = region.toSelection().contains(e.getFrom());
		boolean toInRegion   = region.toSelection().contains(e.getTo());
		
		// Handle player moving in/out of KOTH region
		if (fromInRegion && !toInRegion)
			man.getGame().onPlayerLeaveRegion(e.getPlayer());
		else if (!fromInRegion && toInRegion)
			man.getGame().onPlayerEnterRegion(e.getPlayer());
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		// Ignore if no game is running
		if (!man.isGameRunning())
			return;
		
		// Ignore if region is undefined in config
		SerialSelection region = man.getPlugin().getConfiguration().region;
		if (region == null) {
			handleNullRegion();
			return;
		}
		
		// Ignore if player is not already inside of the region
		if (!region.toSelection().contains(e.getPlayer().getLocation()))
			return;
		
		man.getGame().onPlayerLeaveRegion(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		// Ignore if no game is running
		if (!man.isGameRunning())
			return;
		
		// Ignore if region is undefined in config
		SerialSelection region = man.getPlugin().getConfiguration().region;
		if (region == null) {
			handleNullRegion();
			return;
		}
		
		// Ignore if player is not already inside of the region
		if (!region.toSelection().contains(e.getEntity().getLocation()))
			return;
		
		man.getGame().onPlayerLeaveRegion(e.getEntity());
	}
	
	private void handleNullRegion() {
		// Only inform of undefined region once (would spam otherwise)
		if (!informedOfError) {
			Bukkit.getLogger().warning("Region for KOTH is not defined! Please set it using the in-game commands.");
			informedOfError = true;
		}
	}
	
}
