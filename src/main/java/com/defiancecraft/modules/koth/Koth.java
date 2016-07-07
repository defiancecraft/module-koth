package com.defiancecraft.modules.koth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import be.maximvdw.placeholderapi.PlaceholderAPI;

import com.defiancecraft.core.command.CommandRegistry;
import com.defiancecraft.core.modules.impl.JavaModule;
import com.defiancecraft.modules.koth.commands.AdminCommands;
import com.defiancecraft.modules.koth.config.KothConfig;
import com.defiancecraft.modules.koth.game.Game;
import com.defiancecraft.modules.koth.game.GameManager;
import com.defiancecraft.modules.koth.listeners.PlayerMovementListener;
import com.defiancecraft.modules.koth.utils.DurationFormatter;

public class Koth extends JavaModule {

	private KothConfig config;
	private GameManager man;
	
    public void onEnable() {
    	
    	// Initial config setup
    	this.reloadConfiguration();
    	
    	// Create game manager and schedule first game
    	this.man = new GameManager(this);
    	this.man.scheduleNextGame();
    	
    	// Register listeners
    	this.getServer().getPluginManager().registerEvents(new PlayerMovementListener(man), this);
    	
    	// Register commands
    	AdminCommands adminCmds = new AdminCommands(this);
    	CommandRegistry.registerPlayerCommand(this, "koth", "defiancecraft.koth.help", adminCmds::help);
    	CommandRegistry.registerPlayerSubCommand("koth", "help", "defiancecraft.koth.help", adminCmds::help);
    	CommandRegistry.registerPlayerSubCommand("koth", "setregion", "defiancecraft.koth.setregion", adminCmds::setRegion);
    	CommandRegistry.registerPlayerSubCommand("koth", "setconsolation", "defiancecraft.koth.setconsolation", adminCmds::setConsolation);
    	CommandRegistry.registerPlayerSubCommand("koth", "createtable", "defiancecraft.koth.createtable", adminCmds::createTable);
    	CommandRegistry.registerPlayerSubCommand("koth", "deletetable", "defiancecraft.koth.deletetable", adminCmds::deleteTable);
    	CommandRegistry.registerPlayerSubCommand("koth", "addloot", "defiancecraft.koth.addloot", adminCmds::addLoot);
    	
    	// Register placeholders
    	if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
    		
    		/* koth_next_timer: duration until next KOTH event */ 
    		PlaceholderAPI.registerPlaceholder(this, "koth_next_timer", (event) -> {
    			return DurationFormatter.formatDuration(this.man.getTimeRemainingUntilStart());
    		});
    		
    		/* koth_winner: winner of current KOTH game */
    		PlaceholderAPI.registerPlaceholder(this, "koth_winner", (event) -> {
    			
    			Game game = this.man.getGame();
    			// Ensure game is running
    			if (game == null)
    				return this.config.lang.noWinnerString;
    			
    			Player winner = game.getWinningPlayer();
    			// Ensure winner is existent
    			if (winner == null)
    				return this.config.lang.noWinnerString;
    			
    			return winner.getName();
    			
    		});
    		
    		/* koth_win_timer: duration until winner wins */
    		PlaceholderAPI.registerPlaceholder(this, "koth_win_timer", (event) -> {
    			if (this.man.getGame() == null)
    				return "";
    			
    			return this.man.getGame().getTimeRemainingUntilWin().getSeconds() + "s";
    		});
    		
    	}
    	
    }
    
    public void onDisable() {
    	this.man.stopGame(false);
    }
    
    public GameManager getGameManager() {
    	return man;
    }
    
    public KothConfig getConfiguration() {
    	return config;
    }
    
    public boolean saveConfiguration() {
    	return saveConfig(config);
    }
    
    public void reloadConfiguration() {
    	this.config = getConfig(KothConfig.class);
    }
    
}
