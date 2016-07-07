package com.defiancecraft.modules.koth.game;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.maximvdw.airbar.api.AirBarAPI;

import com.defiancecraft.modules.koth.Koth;
import com.defiancecraft.modules.koth.config.KothConfig;
import com.defiancecraft.modules.koth.config.KothConfig.LootTable;
import com.defiancecraft.modules.koth.config.KothConfig.LootTableItem;
import com.defiancecraft.modules.koth.game.tasks.WinnerHasWonTask;
import com.defiancecraft.modules.koth.game.tasks.WinnerParticlesTask;

public class Game {

	// TODO doc
	private final Koth plugin;
	
	/**
	 * A set of players inside the KOTH region, in the order
	 * that they entered. This way, top of the set will be the
	 * 'winning' player.
	 */
	private LinkedHashSet<UUID> active = new LinkedHashSet<>();
	
	/**
	 * A set of participants, i.e. all of those who entered
	 * the region at one point or another.
	 */
	private Set<UUID> participants = new HashSet<>();
	
	/**
	 * The loot table, selected in constructor.
	 */
	private LootTable lootTable;
	
	/**
	 * The consolatory loot table, also selected in constructor.
	 */
	private LootTable consolationTable;
	
	/**
	 * The KOTH config to use; must stay consistent through the lifetime,
	 * otherwise things fuck up - e.g. loot tables, timings must stay consistent
	 */
	private final KothConfig config;
	
	/**
	 * Whether the game is currently running
	 */
	private boolean running = false;
	
	/**
	 * The previously winning player
	 */
	private UUID winner;
	
	// TODO doc
	private WinnerHasWonTask winnerHasWonTask;
	
	// TODO doc
	private Instant winnerHasWonInstant;
	
	// TODO doc
	private WinnerParticlesTask winnerParticlesTask;
	
	// TODO doc
	private boolean airbarEnabled = false;
	
	/**
	 * Creates a new Game instance of King of the Hill, selecting
	 * an appropriate loot table from the given configuration according
	 * to the number of players.
	 * 
	 * @param config
	 * @param initialPlayers
	 */
	public Game(Koth plugin, int initialPlayers) {
		this.plugin = plugin;
		this.config = plugin.getConfiguration();
		
		// First ensure that the number of initialPlayers is acceptable
		if (initialPlayers < this.config.minimumPlayers)
			throw new IllegalStateException("Not enough players to create a game!");
		
		// Iterate over configured loot tables and select/set best applicable
		// loot table AND consolation table
		for (LootTable table : config.lootTables) {

			// Ensure we have the right number of players
			if (table.minPlayers >= initialPlayers)
				continue;
			
			// If this is a consolation table, treat it as such
			if (config.consolationTables.contains(table.name)) {
				
				// Set if: a) the current table is not set
				//     or  b) min players of this is higher than current table's min players (and so this table is better)
				if (this.consolationTable == null
						||this.consolationTable.minPlayers < table.minPlayers)
					this.consolationTable = table;
				
			// Otherwise, this must be a loot table
			} else {
				
				// Set if: a) the current table is not set
				//     or  b) min players of this is higher than current table's min players (and so this table is better)
				if (this.lootTable.minPlayers < table.minPlayers
						|| this.lootTable == null)
					this.lootTable = table;				
			}
			
		}
		
		// If no loot table was found, inform the server administrators, but continue
		// (No need to inform about consolation table - more often not desired)
		if (lootTable == null) {
			Bukkit.getLogger().warning(
				"No loot table could be found for the newly created game of KOTH!\n" +
				"This is likely due to a misconfiguration. Please add appropriate loot\n" +
				"table entries for the number of players: " + initialPlayers + "\n" +
				"Note that the winners of this game will receive NO LOOT!"
			);
		}
		
		// Check whether AirBar will be enabled
		if (Bukkit.getPluginManager().isPluginEnabled("AirBar"))
			airbarEnabled = true;
		
	}
	
	void start() {
		
		// Inform players that the game is starting
		Bukkit.broadcastMessage(formatMessage(config.lang.start, ""));
		
		// Set airbar to 'alternative'
		if (airbarEnabled)
			for (Player player : Bukkit.getOnlinePlayers())
				AirBarAPI.showAirBar(player, config.alternativeAirbar);
		
		// Update running status
		this.running = true;
	}

	void stop(boolean awardPlayers) {
		
		// Update running status
		this.running = false;
		
		Player winner = getWinningPlayer();
		
		// Inform players that the game is over and of winner
		Bukkit.broadcastMessage(formatMessage(config.lang.end, winner == null ? config.lang.noWinnerString : winner.getName()));
		
		// Set airbar back to default
		if (airbarEnabled)
			for (Player player : Bukkit.getOnlinePlayers())
				AirBarAPI.showAirBar(player, config.defaultAirbar);
		
		if (awardPlayers) {
			
			// If there was a winner, and they are online, award them `lootAmount`
			// items from the `lootTable` based on the items' weights/probabilities
			if (winner != null) {
				giveRandomLoot(winner, lootTable, config.lootAmount);
			}
			
			// Award each of the participants that are online `consolationAmount` items
			// from the `consolationTable` based on the items' weights/probabilities
			for (UUID uuid : participants) {
				Player participant = Bukkit.getPlayer(uuid);
				
				// Ensure participant is online
				if (participant != null && participant.isOnline()) {
					// Give consolation loot and inform them if any was received
					if (giveRandomLoot(participant, consolationTable, config.consolationAmount))
						participant.sendMessage(formatMessage(config.lang.consolation, ""));
				}
			}
			
		}
		
	}
	
	public boolean isRunning() {
		return running;
	}
	
	// TODO doc
	public Duration getTimeRemainingUntilWin() {
		if (winnerHasWonInstant == null)
			return Duration.ZERO;
		
		Duration dur = Duration.between(Instant.now(), winnerHasWonInstant);
		if (dur.isNegative())
			return Duration.ZERO;
		
		return dur;
	}
	
	public Player getWinningPlayer() {
		if (active.size() < 1)
			return null;
		
		UUID current = null;
		
		for (Iterator<UUID> iter = active.iterator(); iter.hasNext(); current = iter.next()) {
			Player player = Bukkit.getPlayer(current);
			
			// Only look for winners that are online; don't bother
			// removing those that aren't - this should be handled by events
			// Note that the player should be the first, as this iterator iterates in
			// order of insertion (as it is a LinkedHashSet)
			if (player != null && player.isOnline())
				return player;
		}
		
		// Either all players are dead, or nobody is winning
		return null;
	}

	public LootTable getLootTable() {
		return this.lootTable;
	}
	
	public LootTable getConsolationTable() {
		return this.consolationTable;
	}
	
	public void onWinnerChanged() {
		if (!running)
			return;
		
		// We want sequential winner updates, not concurrent
		// updates of the winner! (This method is likely called by
		// an asynchronous event thread)
		synchronized (winner) {
			Player oldWinner = winner == null ? null : Bukkit.getPlayer(winner);
			Player newWinner = getWinningPlayer();
			
			// Ensure winner has actually changed
			if (newWinner.getUniqueId().equals(winner))
				return;
			
			// Announce that winner has changed
			Bukkit.broadcastMessage(formatMessage(config.lang.winnerChanged, newWinner.getName()));
			
			// Inform oldWinner they're now losing
			if (oldWinner != null && oldWinner.isOnline()) {
				oldWinner.sendMessage(formatMessage(config.lang.losing, newWinner.getName()));
			}
			
			// End oldWinner's tasks
			if (winnerHasWonTask != null)
				winnerHasWonTask.cancel();
			
			if (winnerParticlesTask != null)
				winnerParticlesTask.cancel();
			
			// Inform newWinner they're now winning
			newWinner.sendMessage(formatMessage(config.lang.winning, newWinner.getName()));
	
			// Start newWinner's tasks
			this.winnerHasWonTask = new WinnerHasWonTask(plugin);
			this.winnerHasWonTask.runTaskLater(plugin, config.winTimeSeconds * 20);
			this.winnerHasWonInstant = Instant.now().plus(Duration.of(config.winTimeSeconds, ChronoUnit.SECONDS));
			this.winnerParticlesTask = new WinnerParticlesTask(config, newWinner);
			this.winnerParticlesTask.runTaskTimer(plugin, 0L, config.winParticleFrequencyTicks);
			
			// Update the winner
			this.winner = newWinner.getUniqueId();
		}
		
	}
	
	public void onPlayerEnterRegion(Player player) {
		if (!running)
			return;
		
		synchronized (active) {
			active.add(player.getUniqueId());
			participants.add(player.getUniqueId());
			
			if (active.size() == 1)
				onWinnerChanged();
		}
	}
	
	public void onPlayerLeaveRegion(Player player) {
		if (!running)
			return;
		
		synchronized (active) {
			active.remove(player.getUniqueId());
		}
		
		if (active.size() > 0)
			onWinnerChanged();
	}
	
	protected boolean giveRandomLoot(Player recipient, LootTable table, int amount) {
		
		// Sanity checks
		if (amount < 0 || recipient == null || !recipient.isOnline() || table == null)
			return false;

		Random rand = new Random();
		List<LootTableItem> items = new ArrayList<>(table.items);
		List<ItemStack> awardedItems = new ArrayList<>();
		
		// Give `amount` items
		for (int i = 0; i < amount; i++) {
			
			double roll  = rand.nextDouble();
			double total = 0;
		
			// Find the item in which the rarity of this item falls within,
			// e.g. if an item has 10% rarity, is the first item in `items`, and
			// the `roll` fell between 0 <= x < 10, the item would be chosen
			for (LootTableItem item : items) {
				if (roll >= total && roll < item.rarity) {
					ItemStack is = item.item.toItemStack();
					awardedItems.add(is);
					break;
				}
				total += item.rarity;
			}
			
		}
		
		Map<Integer, ItemStack> dropItems = recipient.getInventory().addItem(awardedItems.toArray(new ItemStack[] {}));
		if (dropItems.size() > 0) {
			// Drop items that could not fit in inventory
			dropItems.values().forEach((item) ->
				recipient.getWorld().dropItem(recipient.getLocation(), item)
			);
			// Notify player of dropped items
			recipient.sendMessage(formatMessage(config.lang.noSpace, "" /* We -shouldn't- need to substitute the winning player here */));
		}
		
		return awardedItems.size() > 0;
		
	}
	
	protected String formatMessage(String message) {
		return formatMessage(message, getWinningPlayer().getName());
	}
	
	protected String formatMessage(String message, String winningPlayer) {
		message = message == null ? "" : message;
		return ChatColor.translateAlternateColorCodes('&', message
			.replace("{winner}", winningPlayer)
			.replace("{loot_friendlyname}", lootTable.friendlyName)
		);
	}
	
}
