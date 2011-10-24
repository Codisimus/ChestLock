package com.codisimus.plugins.chestlock;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * A Safe is a Chest, Furnace, or Dispenser which can be locked
 *
 * @author Codisimus
 */
public class Safe {
    public Block block;
    public String owner;
    public boolean locked = true;
    public boolean lockable = true;
    public LinkedList<String> coOwners = new LinkedList<String>();
    public LinkedList<String> groups = new LinkedList<String>();

    /**
     * Constructs a new Safe
     * 
     * @param owner The name of the owner of the Safe
     * @param safe The Block of the Safe
     * @param lockable The boolean value of whether the Safe is lockable
     * @param coOwners The list of the CoOwners of the Safe
     * @param groups The list of the CoOwner groups of the Safe
     */
    public Safe(String owner, Block block, boolean lockable, LinkedList<String> coOwners, LinkedList<String> groups) {
        this.owner = owner;
        this.block = block;
        this.lockable = lockable;
        this.coOwners = coOwners;
        this.groups = groups;
    }
    
    /**
     * Constructs a new Safe
     * 
     * @param owner The name of the owner of the Safe
     * @param safe The Block of the Safe
     */
    public Safe(String owner, Block block) {
        this.owner = owner;
        this.block = block;
    }

    /**
     * Returns whether the given Block is left or right of the Safe Block
     * Will only return true is both the Block and the Safe are Chests
     * 
     * @param block The Block of the Safe
     * @return true if the given Block is left or right of the Safe Block
     */
    public boolean isNeighbor(Block safe) {
        if (block.getTypeId() != 54 || safe.getTypeId() != 54)
            return false;
        
        if (block.getY() != safe.getY())
            return false;
        
        if (block.getWorld() != safe.getWorld())
            return false;
        
        int a = block.getX();
        int c = block.getZ();
        int x = safe.getX();
        int z = safe.getZ();
        if (a == x)
            return c == z+1 || c == z-1;
        else if (c == z)
            return a == x+1 || a == x-1;
        else
            return false;
    }

    /**
     * Returns whether the given player is a CoOwner
     * CoOwner includes being in a group that has CoOwnership
     * 
     * @param player The Player to be check for CoOwnership
     * @return true if the given player is a CoOwner
     */
    public boolean isCoOwner(Player player) {
        //Check to see if the Player is a CoOwner
        for (String coOwner: coOwners)
            if (coOwner.equalsIgnoreCase(player.getName()))
                return true;

        //Check to see if the Player is in a group that has CoOwnerShip
        for (String group: groups)
            if (ChestLock.permissions.getUser(player).inGroup(group))
                return true;
        
        return false;
    }
    
    /**
     * Checks if the given Player has owner rights to the Safe
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
