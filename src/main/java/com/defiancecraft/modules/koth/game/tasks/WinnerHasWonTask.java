package com.defiancecraft.modules.koth.game.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.koth.Koth;

public class WinnerHasWonTask extends BukkitRunnable {

	private Koth plugin;
	
	public WinnerHasWonTask(Koth plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		plugin.getGameManager().stopGame(true);
	}
	
}
