package com.codisimus.plugins.chestlock.listeners;

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
        Block block = event.getBlock();
        
        LockedDoor lockedDoor = SaveSystem.findDoor(block);
        if (lockedDoor == null)
            return;
        
        Door door = (Door)lockedDoor.block.getState().getData();
        
        //Allows redstone to close a door but not open it
        if (!door.isOpen() && lockedDoor.key != 0)
            event.setNewCurrent(event.getOldCurrent());
    }

    @Override
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        LockedDoor door = SaveSystem.findDoor(block);
        
        if (door != null)
            if (door.isOwner(player))
                SaveSystem.doors.remove(door);
            else
                event.setCancelled(true);
        else {
            Safe safe = SaveSystem.findSafe(block);

            if (safe == null)
                return;

            if (safe.isOwner(player))
                SaveSystem.removeSafe(safe);
            else
                event.setCancelled(true);
        }
    }
}
