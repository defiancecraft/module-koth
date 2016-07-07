package com.defiancecraft.modules.koth.config.components;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class SerialItemStack {

	public String id;
	public int quantity;
	public short damage;
	public String displayName;
	public List<String> lore;
	
	/**
	 * List of enchantments in format NAME:LEVEL
	 */
	public List<String> enchantments;
	
	public SerialItemStack(ItemStack item) {
		this.id = item.getType().name();
		this.quantity = item.getAmount();
		this.damage = item.getDurability();
		this.displayName = item.getItemMeta().getDisplayName();
		this.lore = item.getItemMeta().getLore();
	}
	
	public ItemStack toItemStack() {
		ItemStack ret = new ItemStack(Material.getMaterial(id), quantity, damage);
		
		// Add display name if present
		if (displayName != null) ret.getItemMeta().setDisplayName(displayName);
		
		// Add lore if present
		if (lore != null) ret.getItemMeta().setLore(lore);
		
		// Add enchantments if present
		if (enchantments != null) {
			for (String enchantment : enchantments) {				 
				try {
					ret.addUnsafeEnchantment(
						Enchantment.getByName(enchantment.split(":")[0]), // Possible NPE (getByName is nullable)
						Integer.parseInt(enchantment.split(":")[1]) // Possible NumberFormatException (parseInt), ArrayIndexOutOfBoundsException (split can return len 1 array)
					);
				} catch (Exception e) {
					System.err.printf(
						"Error parsing enchantment: '%s'; exception: %s - %s\n",
						enchantment,
						e.getClass().getSimpleName(),
						e.getMessage()
					);
					continue;
				}
			}
		}
		
		return ret;
	}
	
}
