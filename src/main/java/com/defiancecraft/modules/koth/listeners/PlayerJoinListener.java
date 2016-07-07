package com.defiancecraft.modules.koth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import be.maximvdw.airbar.api.AirBarAPI;

import com.defiancecraft.modules.koth.Koth;

public class PlayerJoinListener implements Listener {

	private Koth plugin;
	
	public PlayerJoinListener(Koth plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (plugin.getGameManager().isGameRunning())
			AirBarAPI.showAirBar(e.getPlayer(), plugin.getConfiguration().alternativeAirbar);
		else
			AirBarAPI.showAirBar(e.getPlayer(), plugin.getConfiguration().defaultAirbar);
	}
	
}
