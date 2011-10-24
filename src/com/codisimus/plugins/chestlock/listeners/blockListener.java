package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.LockedDoor;
import com.codisimus.plugins.chestlock.Safe;
import com.codisimus.plugins.chestlock.SaveSystem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Door;

/**
 * Listens for griefing events
 * 
 * @author Codisimus
 */
public class blockListener extends BlockListener {

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        //Allow if Block is not a LockedDoor
        LockedDoor lockedDoor = SaveSystem.findDoor(event.getBlock());
        if (lockedDoor == null)
            return;
        
        Door door = (Door)lockedDoor.block.getState().getData();
        
        //Allow Redstone to close a Door but not open it
        if (!door.isOpen() && lockedDoor.key != 0)
            event.setNewCurrent(event.getOldCurrent());
    }

    @Override
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        
        //Check if the Block is a LockedDoor
        LockedDoor door = SaveSystem.findDoor(block);
        if (door != null) {
            //Cancel the event if the Player is not the Owner of the LockedDoor or has the admin node
            if (!player.getName().equals(door.owner) && !ChestLock.hasPermission(player, "admin")) {
                event.setCancelled(true);
                return;
            }
            
            //Delete the LockedDoor from the saved data
            SaveSystem.doors.remove(door);
            return;
        }
        
        //Return if the Block is not a Safe
        Safe safe = SaveSystem.findSafe(block);
        if (safe == null)
            return;

        //Cancel the event if the Player is not the Owner of the Safe
        if (safe.isOwner(player)) {
            event.setCancelled(true);
            return;
        }
        
        //Delete the Safe from the saved data
        SaveSystem.removeSafe(safe);
        return;
    }
}
