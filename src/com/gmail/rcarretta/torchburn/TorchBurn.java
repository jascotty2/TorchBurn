package com.gmail.rcarretta.torchburn;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftWorld;
import net.minecraft.server.EnumSkyBlock;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class TorchBurn extends JavaPlugin {

    protected static TorchBurn myPlugin;
    protected static int intensity = 15;
    protected static int falloff = 3;
    private static boolean slowServer = true;
    private final TorchBurnPlayerListener playerListener = new TorchBurnPlayerListener();
    private final TorchBurnEntityListener entityListener = new TorchBurnEntityListener();
    private final TorchBurnBlockListener blockListener = new TorchBurnBlockListener();
    // used to reduce calls to lightArea(), instead of on every playermove, only when block changes.
    private static HashMap<Player, TorchBurnSimplePlayerLoc> playerLoc = new HashMap<Player, TorchBurnSimplePlayerLoc>();
    private static HashMap<Location, TorchBurnLightLevelOwner> prevState = new HashMap<Location, TorchBurnLightLevelOwner>();
    private static HashMap<Player, List<Location>> playerBlocks = new HashMap<Player, List<Location>>();

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        myPlugin = this;
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
    }

    @Override
    public void onDisable() {
    }

    public static void extinguish(Player player, int slot) {
        // remove torches, restore light, etc.
        if (player.getInventory().getItem(slot).getAmount() > 1) {
            // decrement stack
            player.getInventory().getItem(slot).setDurability((short) 0);
            player.getInventory().getItem(slot).setAmount(player.getInventory().getItem(slot).getAmount() - 1);
        } else {
            // last torch
            player.getInventory().removeItem(player.getInventory().getItem(slot));
        }
        removePlayerLoc(player);
        if (playerBlocks.containsKey(player)) {
            unLightarea(player);
            playerBlocks.remove(player);
        }
    }

    // i am so lazy
    public static void extinguishNoRemove(Player player) {
        int slot = player.getInventory().getHeldItemSlot();
        // remove torches, restore light, etc.
        if (player.getInventory().getItemInHand().getAmount() > 1) {
            // decrement stack
            player.getInventory().getItem(slot).setDurability((short) 0);
        } else {
            // last torch
            player.getInventory().removeItem(player.getInventory().getItem(slot));
        }
        removePlayerLoc(player);
        if (playerBlocks.containsKey(player)) {
            unLightarea(player);
            playerBlocks.remove(player);
        }
    }

    public static void extinguish(Player player) {
        int slot = player.getInventory().getHeldItemSlot();
        // remove torches, restore light, etc.
        if (player.getInventory().getItemInHand().getAmount() > 1) {
            // decrement stack
            player.getInventory().getItem(slot).setDurability((short) 0);
            player.getInventory().getItem(slot).setAmount(player.getInventory().getItem(slot).getAmount() - 1);
        } else {
            // last torch
            player.getInventory().removeItem(player.getInventory().getItem(slot));
        }
        removePlayerLoc(player);
        if (playerBlocks.containsKey(player)) {
            unLightarea(player);
            playerBlocks.remove(player);
        }
    }

    public static boolean updatePlayerLoc(Player player) {
        Location loc = player.getLocation();
        TorchBurnSimplePlayerLoc tbLoc = playerLoc.get(player);

        if (tbLoc == null) {
            return false;
        }

        if (tbLoc.equals(loc)) {
            return false;
        }

        tbLoc.set(loc.clone());
        return true;
    }

    public static boolean isLit(Player player) {
        return (playerLoc.containsKey(player));
    }

    public static void addPlayerLoc(Player player) {
        Location loc = player.getLocation();
        playerLoc.put(player, new TorchBurnSimplePlayerLoc(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public static void removePlayerLoc(Player player) {
        playerLoc.remove(player);
    }

    public static void lightTorch(Player player) {
        lightArea(player, intensity, falloff);
        addPlayerLoc(player);
        myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(myPlugin, new TorchBurnSchedule(player), 0);
    }

    public static void lightArea(Player player, int intensity, int falloff) {
        assert (intensity >= 0 && intensity <= 15 && falloff > 0 && falloff <= 15);
        
        CraftWorld world = (CraftWorld) player.getWorld();
        int radius = intensity / falloff;
        int blockX = player.getLocation().getBlockX();
        int blockY = player.getLocation().getBlockY();
        int blockZ = player.getLocation().getBlockZ();

        // first reset all light around
        if (playerBlocks.containsKey(player)) {
            unLightarea(player);
            playerBlocks.remove(player);
        }

        List<Location> blockList = new ArrayList<Location>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int newIntensity;
                    int curIntensity = world.getHandle().j(blockX + x, blockY + y, blockZ + z);

                    if (slowServer == true) {
                        // this is fast
                        newIntensity = (intensity - (Math.abs(x) + Math.abs(y) + Math.abs(z))) < 0 ? 0 : intensity - (Math.abs(x) + Math.abs(y) + Math.abs(z));
                    } else {
                        // this is slow, but nicer
                        Vector origin = new Vector(blockX, blockY, blockZ);
                        Vector v = new Vector(blockX + x, blockY + y, blockZ + z);
                        if (v.isInSphere(origin, radius)) {
                            // looks like the entry is within the radius
                            double distanceSq = v.distanceSquared(origin);
                            newIntensity = (int) (((intensity - Math.sqrt(distanceSq) * falloff) * 100 + 0.5) / 100);
                        } else {
                            newIntensity = curIntensity;
                        }
                    }

                    TorchBurnLightLevelOwner prevIntensity;
                    Location l = new Location(world, blockX + x, blockY + y, blockZ + z);
                    prevIntensity = TorchBurn.prevState.get(l);
                    int worldIntensity = world.getHandle().j(blockX + x, blockY + y, blockZ + z);
                    if (prevIntensity != null) {
                        // this area was in the map already. see if we are brightening and if it belongs to us
                        if (prevIntensity.getLevel() < newIntensity && !(prevIntensity.getPlayer().equals(player))) {
                            // we are brightening, remove the other guy's entry and add our own
                            TorchBurn.prevState.remove(l);
                            TorchBurn.prevState.put(l, new TorchBurnLightLevelOwner(player, worldIntensity));
                        }
                    } else {
                        // add the current world's light level to the map
                        TorchBurn.prevState.put(l, new TorchBurnLightLevelOwner(player, world.getHandle().j(blockX + x, blockY + y, blockZ + z)));
                    }
                    // light 'em up!
                    if (newIntensity > worldIntensity) {
//						if my pull request to bukkit gets accepted
//						l.getBlock().setLightLevel(newIntensity);
                        world.getHandle().b(EnumSkyBlock.BLOCK, blockX + x, blockY + y, blockZ + z, newIntensity);
                    }

                    blockList.add(l);
                }
            }
        }
        playerBlocks.put(player, blockList);
    }

    public static void unLightarea(Player player) {
        TorchBurnLightLevelOwner lightLevelOwner;
        for (Location l : playerBlocks.get(player)) {
            lightLevelOwner = prevState.get(l);
            if (lightLevelOwner != null) {
                if (lightLevelOwner.getPlayer().equals(player)) {
// this is if my pull request to bukkit gets accepted
//					l.getBlock().setLightLevel(lightLevelOwner.getLevel());
                    ((CraftWorld) (player.getWorld())).getHandle().b(EnumSkyBlock.BLOCK, l.getBlockX(), l.getBlockY(), l.getBlockZ(), lightLevelOwner.getLevel());
                    prevState.remove(l);
                }
            }
        }
    }
}
