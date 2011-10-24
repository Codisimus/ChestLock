package com.codisimus.plugins.chestlock;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * A LockedDoor is a Block that requires a key to open
 * 
 * @author Codisimus
 */
public class LockedDoor {
    public Block block;
    public String owner;
    public int key;

    /**
     * Constructs a new LockedDoor
     * 
     * @param owner The name of the owner of the door
     * @param block The Block of the door
     * @param key The item which must be in the players hand to open the door
     */
    public LockedDoor(String owner, Block block, int key) {
        this.owner = owner;
        this.block = block;
        this.key = key;
    }

    /**
     * Returns whether the given block is above or below the door Block
     * 
     * @param check The Block of the door
     * @return true if the given block is above or below the door Block
     */
    public boolean isNeighbor(Block check) {
        //Return false if Blocks are not in the same x-axis, z-axis, or World
        if (block.getX() != check.getX() || block.getZ() != check.getZ() || block.getWorld() != check.getWorld())
            return false;
        
        //Return true if the Blocks are stacked right on top of each other
        int a = block.getY();
        int b = check.getY();
        return a == b+1 || a == b-1;
    }

    /**
     * Returns true if the key is air or the player is holding the key
     * Returns true if the player has the admin permission and is holding the global key
     * 
     * @param player The player who may have the key
     * @return whether the player has the required key
     */
    public boolean hasKey(Player player) {
        //Return true if the door is unlockable
        if (key == 0)
            return true;
        
        int holding = player.getItemInHand().getTypeId();
        
        //Return true if the Player is holding the key
        if (holding == key)
            return true;
        
        //Return true if the Player is an admin and holding the global key
        return ChestLock.hasPermission(player, "admin") && holding == ChestLock.global;
    }
    
    /**
     * Checks if the given Material ID is an Iron Door
     *
     * @param id The Material ID to be checked
     * @return true if the Material is an Iron Door
     */
    public static boolean isIron(int id) {
        switch (id) {
            case 71: return true;
            case 330: return true;
            default: return false;
        }
    }
    
    /**
     * Checks if the given Player has owner rights to the LockedDoor
     *
     * @param player The given Player
     * @return true if the Player is the owner or has the admin node
     */
    public boolean isOwner(Player player) {
        if (player == null)
            return false;
        
        if (player.getName().equals(owner))
            return true;
        
        return ChestLock.hasPermission(player, "admin");
    }
}
