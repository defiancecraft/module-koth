package com.defiancecraft.modules.koth.game.tasks;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.defiancecraft.modules.koth.config.KothConfig;
import com.defiancecraft.modules.koth.particle.Particle;
import com.defiancecraft.modules.koth.particle.ParticleWorld;

public class WinnerParticlesTask extends BukkitRunnable {

	private KothConfig config;
	private Player player;
	private ParticleWorld world = null;
	
	public WinnerParticlesTask(KothConfig config, Player player) {
		this.config = config;
		this.player = player;
		try {
			this.world = new ParticleWorld(player.getWorld());
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassNotFoundException e) {
			Bukkit.getLogger().warning(String.format("Failed to create WinnerParticlesTask - could not create ParticleWorld; exception %s - %s", e.getClass().getName(), e.getMessage()));
		}
	}
	
	public void run() {
		if (player == null || !player.isOnline() || world == null)
			return;
		
		for (String particleString : config.winParticles) {
			String particleName = particleString;
			int count = 1;
			
			// If particle string contains a colon and there are two parts, parse
			// it as PARTICLE_NAME:COUNT
			if (particleString.contains(":") && particleString.split(":").length == 2) {
				try {
					particleName = particleString.split(":")[0];
					count = Integer.parseInt(particleString.split(":")[1]);
					
					// Ensure parsed count is valid
					if (count < 1)
						count = 1;
				} catch (NumberFormatException e) {}
			}
			
			Particle particle = Particle.valueOf(particleName);
			
			if (particle == null) {
				Bukkit.getLogger().warning("Malformed particle name: " + particleName);
				continue;
			}
			
			if (!particle.getDataType().equals(Void.class)) {
				Bukkit.getLogger().warning("Particles with data are not supported: " + particle.name());
				continue;
			}
			
			world.spawnParticle(particle, player.getLocation(), count);   
		}
	}
	
}
