package com.defiancecraft.modules.koth.game;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.bukkit.Bukkit;

import be.maximvdw.airbar.AirBar;
import be.maximvdw.airbar.api.AirBarAPI;

import com.defiancecraft.modules.koth.Koth;
import com.defiancecraft.modules.koth.game.tasks.StartGameTask;

public class GameManager {

	private Koth plugin;
	private StartGameTask startTask;
	private Game currentGame;
	
	private Instant nextStartTask;
	
	public GameManager(Koth plugin) {
		this.plugin = plugin;
	}
	
	public void scheduleNextGame() {
		// Ensure a game is not running; otherwise, this start
		// could coincide with the current game!
		if (!isGameRunning()) {
			// We must create a NEW start task every time we run it... (can't reschedule)
			this.startTask = new StartGameTask(this);
			this.startTask.runTaskLater(plugin, plugin.getConfiguration().intervalSeconds * 20 /* seconds -> ticks */);
			this.nextStartTask = Instant.now().plus(Duration.of(plugin.getConfiguration().intervalSeconds, ChronoUnit.SECONDS));
		}
	}
	
	public Koth getPlugin() {
		return plugin;
	}
	
	public Game getGame() {
		return currentGame;
	}
	
	public Duration getTimeRemainingUntilStart() {
		if (nextStartTask == null || isGameRunning())
			return Duration.ZERO;
		
		Duration ret = Duration.between(Instant.now(), nextStartTask);
		if (ret.isNegative())
			return Duration.ZERO;
		
		return ret;
	}
	
	public boolean isGameRunning() {
		return currentGame != null && currentGame.isRunning();
	}
	
	/**
	 * Starts a new game. The calling function should ensure that a game
	 * is not already in progress (see {@link #isGameRunning()}), otherwise
	 * an {@link IllegalStateException} will be thrown
	 * <p>
	 * The function will check to ensure that the minimum number of players
	 * required to start a game are present, and, if not, return false. If there
	 * are enough players, a new game instance shall be created and started, and
	 * true is returned.
	 * 
	 * @return Whether a new game was started
	 * @throws IllegalStateException if a game is already in progress
	 */
	public boolean startGame() {
		
		// Don't allow concurrent games
		if (isGameRunning())
			throw new IllegalStateException("Game already in progress. Please stop this game before starting another");
		
		// Don't allow games to run if minimum number of players is not met
		if (Bukkit.getOnlinePlayers().size() < plugin.getConfiguration().minimumPlayers)
			return false;
		
		// Start game with the current number of online players
		this.currentGame = new Game(plugin, Bukkit.getOnlinePlayers().size());
		this.currentGame.start();
		return true;
		
	}
	
	public boolean stopGame(boolean awardPlayers) {
	
		// Ensure game is running
		if (!isGameRunning())
			return false;
		
		this.currentGame.stop(awardPlayers);
		this.scheduleNextGame();
		return true;
		
	}
	
	// NYI:
	// - Game(KothConfig, int)
	// - Game#start
	// - Game#isRunning
	
	// Task scheduling?
	
}
