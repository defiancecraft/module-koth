package com.defiancecraft.modules.koth.game.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.koth.game.GameManager;

public class StartGameTask extends BukkitRunnable {

	private GameManager man;
	
	public StartGameTask(GameManager man) {
		this.man = man;
	}
	
	public void run() {
		// Sanity check - game should not be running, as this is
		// scheduled when game is not running.
		if (!man.isGameRunning())
			if (!man.startGame())
				// Schedule another game start if this one could not happen 
				this.runTaskLater(man.getPlugin(), man.getPlugin().getConfiguration().intervalSeconds * 20);
	}
	
}
