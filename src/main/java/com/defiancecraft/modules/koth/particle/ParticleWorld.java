package com.defiancecraft.modules.koth.particle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ParticleWorld {

	private static final String GET_HANDLE_NAME = "getHandle";
	private static final String SEND_PARTICLES_NAME = "sendParticles";
	
	private World world;
	
	/**
	 * CraftWorld#getHandle() result
	 */
	private Object handle;
	
	/**
	 * NMS/WorldServer#sendParticles
	 */
	private Method sendParticlesMethod;
	
	/**
	 * NMS/EnumParticle#valueOf
	 */
	private Method valueOfMethod;
	
	public ParticleWorld(World world) throws NoSuchMethodException,
											 SecurityException,                      // thanks reflection
											 IllegalAccessException,
											 IllegalArgumentException,
											 InvocationTargetException,
											 ClassNotFoundException {
		this.world = world;
		this.setupReflection();
	}
	
	private void setupReflection() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		// Find CraftWorld#getHandle() and make accessible (even though it should be)
		Method getHandleMethod = world.getClass().getMethod(GET_HANDLE_NAME, new Class<?>[] {});
		getHandleMethod.setAccessible(true);
		
		// Call CraftWorld#getHandle()
		this.handle = getHandleMethod.invoke(world, new Object[] {});
		
		if (handle == null)
			throw new RuntimeException("Could not get handle of world");
		
		// Get NMS classes based on class name of handle
        String packageName = handle.getClass().getName().replaceAll("^(.*)\\.([^\\.]*?)$", "$1");
        Class<?> entityPlayerClass = Class.forName(packageName + ".EntityPlayer");
        Class<?> enumParticleClass = Class.forName(packageName + ".EnumParticle");
        
        this.valueOfMethod = enumParticleClass.getDeclaredMethod("valueOf", new Class<?>[] { String.class });
        this.valueOfMethod.setAccessible(true);
        
        // Get sendParticlesMethod and cache
        this.sendParticlesMethod = handle.getClass().getMethod(SEND_PARTICLES_NAME, new Class<?>[] {
        	entityPlayerClass,
        	enumParticleClass,
        	boolean.class,
        	double.class,
        	double.class,
        	double.class,
        	int.class,
        	double.class,
        	double.class,
        	double.class,
        	double.class,
        	int[].class
        });
        this.sendParticlesMethod.setAccessible(true);
	}
	
	/**
	 * @see org.bukkit.World#spawnParticle(Particle, Location, int)
	 */
    public void spawnParticle(Particle particle, Location location, int count) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, double, double, double, int)
     */
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        spawnParticle(particle, x, y, z, count, null);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, Object)
     */
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, double, double, double, int, Object)
     */
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        spawnParticle(particle, x, y, z, count, 0, 0, 0);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double)
     */
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, double, double, double, int, double, double, double)
     */
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double, Object)
     */
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, double, double, double, int, double, double, double, Object)
     */
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double, double)
     */
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, double, double, double, int, double, double, double, double)
     */
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    /**
     * @see org.bukkit.World#spawnParticle(Particle, Location, int, double, double, double, Object)  
     */
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    /**
     * An implementation of the function of the same descriptor from org.bukkit.World in Bukkit
     * version 1.9.0; this project uses Bukkit 1.8.0, and so it must be implemented manually.
     * <p>
     * Reflection is used to obtain the handle to the underlying NMS class, and the method of
     * this class to spawn particles is similarly obtained and invoked.
     *  
     * @see org.bukkit.World#spawnParticle(Particle, double, double, double, int, double, double, double, double, Object)
     */
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        if (data != null && !particle.getDataType().isInstance(data)) {
            throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
        }

        try {
			sendParticlesMethod.invoke(handle,
				null, // Sender
				particleToNMS(particle), // Particle
				true, // Extended range
				x, y, z, // Position
				count, // Count
				offsetX, offsetY, offsetZ, // Random offset
				extra, // Speed?
				particleToData(particle, data)
			);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			Bukkit.getLogger().warning(String.format("Failed to spawn particle: %s - %s", e.getClass().getName(), e.getMessage()));
		}
        	
    }
    
    /**
     * Taken from toNMS method of CraftParticle, and adapted to use
     * reflection.
     * 
     * @see org.bukkit.craftbukkit.CraftParticle#toNMS(Particle)
     */
    private Object particleToNMS(Particle particle) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return valueOfMethod.invoke(null, particle.name());
    }
    
    /**
     * Taken from toData method of CraftParticle
     * @see org.bukkit.craftbukkit.CraftParticle#toData(Particle, Object)
     */
    @SuppressWarnings("deprecation")
	private static int[] particleToData(Particle particle, Object obj) {
        if (particle.getDataType().equals(Void.class)) {
            return new int[0];
        }
        if (particle.getDataType().equals(ItemStack.class)) {
            if (obj == null) {
                return new int[]{0, 0};
            }
            ItemStack itemStack = (ItemStack) obj;
            return new int[]{itemStack.getType().getId(), itemStack.getDurability()};
        }
        if (particle.getDataType().equals(MaterialData.class)) {
            if (obj == null) {
                return new int[]{0};
            }
            MaterialData data = (MaterialData) obj;
            return new int[]{data.getItemTypeId() + ((int)(data.getData()) << 12)};
        }
        throw new IllegalArgumentException(particle.getDataType().toString());
    }

	
}
