package com.defiancecraft.modules.koth.config;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.defiancecraft.modules.koth.config.components.SerialItemStack;
import com.defiancecraft.modules.koth.config.components.SerialSelection;


public class KothConfig {

	// ----
	//
	// General configuration options
	//
	// ----
	
	/**
	 * How often the Koth event will occur, i.e. interval between event end
	 * and next event start (or on server startup, just the latter).
	 */
	public long intervalSeconds = 60 * 10; 
	
	/**
	 * How long a player must remain the 'king' before winning the game
	 */
	public long winTimeSeconds = 60;
	
	/**
	 * List of particles to spawn around winning player and their count
	 */
	public List<String> winParticles = Arrays.asList("SMOKE_LARGE:5");
	
	/**
	 * How often the particles should be spawned (in ticks)
	 */
	public long winParticleFrequencyTicks = 2;
	
	/**
	 * The region for King of the Hill to take place in,
	 * i.e. where the winners must continue to stand
	 */
	public SerialSelection region = null;
	
	/**
	 * Minimum number of players needed for a game of KOTH to run
	 */
	public int minimumPlayers = 2;
	
	// ----
	// 
	// AirBar plugin config
	//
	// ----
	
	/**
	 * 'Default' airbar to use when KOTH is not running
	 */
	public String defaultAirbar = "default";

	/**
	 * 'Alternative' airbar to use when KOTH is running
	 */
	public String alternativeAirbar = "alt";
	
	// ----
	//
	// Loot/loot tables
	//
	// ----
	
	/**
	 * Amount of items from the selected loot table to award
	 * the winner of the game - these are 'rolled'.
	 */
	public int lootAmount = 5;
	
	/**
	 * Amount of items from a selected consolation table to award
	 * the losers of the game - these are 'rolled'.
	 */
	public int consolationAmount = 2;
	
	/**
	 * List of tables to be used solely as 'consolation' tables; these
	 * will be ignored when selecting a loot table for the winner
	 */
	public List<String> consolationTables = Arrays.asList("consolation");
	
	/**
	 * Loot tables, containing:
	 * <ul>
	 * <li>Items of the loot table
	 * <li>Minimum players required to be online at invocation time of game
	 *     for eligibility for the loot table
	 * <li>Name of loot table to be used in commands
	 * <li>Friendly name of loot table to be displayed at times
	 */
	public List<LootTable> lootTables = Arrays.asList(
			
		// (Example) Default Loot Table
		new LootTable("default", "Default", 2, Arrays.asList(
			new LootTableItem(new SerialItemStack(new ItemStack(Material.IRON_INGOT)), 0.9)
		)),
		
		// (Example) Consolation Loot Table
		new LootTable("consolation", "Consolation", 0, Arrays.asList(
			new LootTableItem(new SerialItemStack(new ItemStack(Material.COAL)), 1)
		))
		
	);
	
	/**
	 * Config section to hold language configuration (i.e. messages)
	 */
	public LanguageConfig lang = new LanguageConfig();
	
	public static class LootTable {
		
		public String name;
		public String friendlyName;
		public int minPlayers;
		public List<LootTableItem> items;
		
		public LootTable(String name, String friendlyName, int minPlayers, List<LootTableItem> items) {
			this.name = name;
			this.friendlyName = friendlyName;
			this.minPlayers = minPlayers;
			this.items = items;
		}
		
	}
	
	public static class LootTableItem {

		public SerialItemStack item;
		public double rarity;
		
		public LootTableItem(SerialItemStack item, double rarity) {
			this.item = item;
			this.rarity = rarity;
		}
		
	}
	
	public static class LanguageConfig {
		
		/**
		 * Message shown to all players at start of KOTH event
		 */
		public String start = "&aKOTH starting! {loot_friendlyname}\n&4Next line";

		/**
		 * Message shown to all players at end of KOTH event
		 */
		public String end = "&aKOTH ending! {winner} won.";
		
		/**
		 * Consolation message shown to participants that did not win
		 */
		public String consolation = "&aYou received consolatory loot. Gratz.";
		
		/**
		 * Message displayed globally when the 'king' changes
		 * {winner}: New winner/king of the hill
		 */
		public String winnerChanged = "&a{winner} is now winning! Loot table: {loot_friendlyname}";

		/**
		 * Shown to players that are now winning
		 */
		public String winning = "You are now winning!";
		
		/**
		 * Shown to players that are now losing
		 */
		public String losing = "You are now losing! {winner} is winning!";
		
		/**
		 * Shown if there is no space in a recipient of a prize's inventory
		 */
		public String noSpace = "There was no space in your inventory, so the items have been dropped.";
		
		/**
		 * What is displayed in place of the winner for placeholders if 'nobody' is winning
		 */
		public String noWinnerString = "Nobody";
		
	}
	
}
