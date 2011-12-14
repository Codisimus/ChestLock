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
     * @param owner The name of the Owner of the Door
     * @param block The Block of the Door
     * @param key The item which must be in the Player's hand to open the Door
     */
    public LockedDoor(String owner, Block block, int key) {
        this.owner = owner;
        this.block = block;
        this.key = key;
    }

    /**
     * Returns whether the given Block is above or below the door Block
     * 
     * @param check The Block of the door
     * @return true if the given Block is above or below the door Block
     */
    public boolean isNeighbor(Block block2) {
        //Return false if Blocks are not in the same x-axis
        if (block.getX() != block2.getX())
            return false;
        
        //Return false if Blocks are not in the same z-axis
        if (block.getZ() != block2.getZ())
            return false;
        
        //Return false if Blocks are not in the same World
        if (block.getWorld() != block2.getWorld())
            return false;
        
        //Return true if the Blocks are stacked right on top of each other
        int a = block.getY();
        int b = block2.getY();
        return a == b+1 || a == b-1;
    }

    /**
     * Returns true if the key is air or the Player is holding the key
     * Returns true if the Player has the admin permission and is holding the global key
     * 
     * @param player The Player who may have the key
     * @return true if the Player has the required key
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
}
