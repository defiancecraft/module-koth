package com.defiancecraft.modules.koth.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_8_R1.Material;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.defiancecraft.core.command.ArgumentParser;
import com.defiancecraft.core.command.ArgumentParser.Argument;
import com.defiancecraft.modules.koth.Koth;
import com.defiancecraft.modules.koth.config.KothConfig.LootTable;
import com.defiancecraft.modules.koth.config.KothConfig.LootTableItem;
import com.defiancecraft.modules.koth.config.components.SerialItemStack;
import com.defiancecraft.modules.koth.config.components.SerialSelection;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class AdminCommands {

	/*
	 * CommandRegistry.registerPlayerCommand(this, "koth", "defiancecraft.koth.help", AdminCommands::help);
    	CommandRegistry.registerPlayerSubCommand("koth", "help", "defiancecraft.koth.help", AdminCommands::help);
    	CommandRegistry.registerPlayerSubCommand("koth", "setregion", "defiancecraft.koth.setregion", AdminCommands::setRegion);
    	CommandRegistry.registerPlayerSubCommand("koth", "createtable", "defiancecraft.koth.createtable", AdminCommands::createTable);
    	CommandRegistry.registerPlayerSubCommand("koth", "addloot", "defiancecraft.koth.addloot", AdminCommands::addLoot);
    	setConsolaton
	 */
	
	private static final String HELP_STRING =
		"&9&lKOTH Help:\n" + 
		"&b/koth help - Show help\n" +
		"&b/koth createtable <name> \"<friendlyName>\" <minPlayers> <consolation (bool)> - Creates a loot table\n" +
		"&b/koth deletetable <name> - Deletes a loot table &lPERMANENTLY\n" +
		"&b/koth setconsolation <table> <bool> - Sets whether a table should be a consolation table\n" +
		"&b/koth setregion - Sets region to WE selection\n" + 
		"&b/koth addloot <table> <rarity> - Adds held ItemStack to a loot table with given rarity\n";
		
	private Koth plugin;
	
	public AdminCommands(Koth plugin) {
		this.plugin = plugin;
	}

	/*
	 * Command: /koth help
	 */
	public boolean help(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', HELP_STRING));
		return true;
	}
	
	/*
	 * Command: /koth createtable <name> "<friendly name>" <minPlayers> <consolation>
	 */
	public boolean createTable(CommandSender sender, String[] args) {
		ArgumentParser parser = new ArgumentParser(String.join(" ", args), Argument.WORD, Argument.STRING, Argument.INTEGER, Argument.WORD);
		if (!parser.isValid()) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cIncorrect command syntax; see help (/koth)"));
			return true;
		}
		
		// Obtain parsed arguments
		String name = parser.getString(1);
		String friendlyName = parser.getString(2);
		int minPlayers = parser.getInt(3);
		boolean consolation = parser.getString(4).equalsIgnoreCase("true");
		
		List<LootTable> lootTables = new ArrayList<>(plugin.getConfiguration().lootTables);
		
		// Ensure loot table is not already existent
		for (LootTable table : lootTables) {
			if (table.name.equalsIgnoreCase(name)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cLoot table already exists!"));
				return true;
			}
		}
		
		// Construct new table and add new loot table
		LootTable table = new LootTable(name, friendlyName, minPlayers, new ArrayList<LootTableItem>());
		plugin.getConfiguration().lootTables = forceAdd(plugin.getConfiguration().lootTables, table);
		
		// Add name to consolation list if consolation
		if (consolation)
			plugin.getConfiguration().consolationTables = forceAdd(plugin.getConfiguration().consolationTables, table.name);
		
		// Save config
		plugin.saveConfiguration();
		
		// Inform sender
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
			String.format("&aTable created:\n" +
				"  &2Name: '&a%s&2'\n" +
				"  &2Friendly Name: '&a%s&2'\n" +
				"  &2Min Players: '&a%d&2'\n" +
				"  &2Consolation: '&a%s&2'\n" +
				"&aNote that the table will not be effective until the next game starts.",
				name, friendlyName, minPlayers, consolation ? "yes" : "no"
			)
		));
		
		return true;
	}
	
	/*
	 * Command: /koth deletetable <name>
	 */
	public boolean deleteTable(CommandSender sender, String[] args) {
		// Ensure correct args
		if (args.length != 1) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid command syntax; see help (/koth)"));
			return true;
		}
		
		String tableName = args[0];
		List<LootTable> newList = new ArrayList<LootTable>(plugin.getConfiguration().lootTables);
		boolean found = false;
		
		// Find table(s) matching name and remove
		for (LootTable table : plugin.getConfiguration().lootTables) {
			if (table.name.equalsIgnoreCase(tableName)) {
				newList.remove(table);
				found = true;
			}
		}
		
		// Find table(s) matching name in consolation tables and remove
		plugin.getConfiguration().consolationTables = forceRemove(plugin.getConfiguration().consolationTables, tableName);
		
		// Set new table list
		plugin.getConfiguration().lootTables = newList;
		
		if (found) {
			plugin.saveConfiguration();
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aLoot table removed! This will take effect on the next game."));
		} else {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cLoot table not found!"));
		}
		
		return true;
	}
	
	/*
	 * Command: /koth setConsolation <table> <bool>
	 */
	public boolean setConsolation(CommandSender sender, String[] args) {
		
		if (args.length != 2) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid command syntax; see help (/koth)"));
			return true;
		}
	
		String tableName = args[0];
		boolean consolation = args[1].equalsIgnoreCase("true");
		
		plugin.getConfiguration().consolationTables = forceChange(plugin.getConfiguration().consolationTables, tableName, !consolation);
		plugin.saveConfiguration();
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aTable was probably set to: " + (consolation ? "" : "not") + " a consolation table."));
		
		return true;
		
	}
	
	/*
	 * Command: /koth setregion
	 */
	public boolean setRegion(CommandSender sender, String[] args) {
		
		Plugin plugin;
		
		// Ensure WorldEdit is enabled
		if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
				|| !((plugin = Bukkit.getPluginManager().getPlugin("WorldEdit")) instanceof WorldEditPlugin)) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cWorldEdit is not enabled!"));
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("You don't look like a player to me.");
			return true;
		}
		
		WorldEditPlugin worldEdit = (WorldEditPlugin)plugin;
		Selection sel = worldEdit.getSelection((Player)sender);
		
		if (sel == null) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou have nothing selected!"));
			return true;
		}
		
		this.plugin.getConfiguration().region = new SerialSelection(sel);
		this.plugin.saveConfiguration();
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aRegion set!"));
		
		return true;
		
	}
	
	public boolean addLoot(CommandSender sender, String[] args) {
		// Ensure they give table and rarity
		if (args.length != 2) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid command syntax; see help (/koth)"));
			return true;
		}
		
		// Ensure they're holding something and are a player
		Player player;
		if (!(sender instanceof Player)
				|| (player = (Player)sender).getItemInHand() == null
				|| player.getItemInHand().getType().equals(Material.AIR)) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou must be holding something that isn't air."));
			return true;
		}
		
		String tableName = args[0];
		String rarityString = args[1];
		double rarity;
		try {
			rarity = Double.parseDouble(rarityString);
		} catch (NumberFormatException e) {
			// Invalidate rarity such that the user will be informed of its invalidity
			rarity = -1;
		}

		// Ensure rarity is valid
		if (rarity <= 0 || rarity > 1) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c'" + rarityString + "' is not a valid rarity."));
			return true;
		}
		
		// Ensure table exists
		int i = 0;
		LootTable table = null;
		boolean found = false;
		for (; i < plugin.getConfiguration().lootTables.size(); i++) {
			
			table = plugin.getConfiguration().lootTables.get(i);
			
			// If find table, break - index will be correct
			if (table.name.equalsIgnoreCase(tableName)) {
				found = true;
				break;
			}
			
		}
		
		if (!found) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cTable does not exist! You can make a new table with /koth createtable"));
			return true;
		}
		
		// Finally, add item to the table
		table.items.add(new LootTableItem(new SerialItemStack(player.getItemInHand()), rarity));
		plugin.getConfiguration().lootTables.set(i, table);
		plugin.saveConfiguration();
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aItem added successfully!"));
		return true;
		
	}
	
	/**
	 * Equivalent to calling {@link #forceChange(List, Object, boolean)}
	 * with `remove` set to false.
	 */
	private <T> List<T> forceAdd(List<T> list, T object) {
		return forceChange(list, object, false);
	}
	
	/**
	 * Equivalent to calling {@link #forceChange(List, Object, boolean)}
	 * with `remove` set to true.
	 */
	private <T> List<T> forceRemove(List<T> list, T object) {
		return forceChange(list, object, true);
	}
	
	/**
	 * 'Forcefully' adds or removes an item to a list; in reality, the object
	 * is attempted to be added. If this is unsuccessful, a new ArrayList is
	 * returned (items can be added to an ArrayList).
	 * <p>
	 * The calling function should then <b>set the list in question to the
	 * returned object</b>. This will ensure it contains the correct item.
	 * 
	 * @param list List to add item to
	 * @param object Object to add
	 * @param remove Whether to remove an object or add it (true to remove)
	 * @return List with item added/removed (either original or new ArrayList)
	 */
	private <T> List<T> forceChange(List<T> list, T object, boolean remove) {
		try {
			if (remove)
				list.remove(object);
			else
				list.add(object);
		} catch (UnsupportedOperationException e) {
			// Clone list to ArrayList, and then add object
			List<T> newList = new ArrayList<>(list);
			if (remove)
				newList.remove(object);
			else
				newList.add(object);
			return newList;
		}
		
		return list;
	}
	
}
