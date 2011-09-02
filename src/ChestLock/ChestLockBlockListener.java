
package ChestLock;

import java.util.LinkedList;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.material.Door;

/**
 *
 * @author Cody
 */
public class ChestLockBlockListener extends BlockListener {

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (ChestLock.isDoor(block.getType())) {
            LinkedList<LockedDoor> doors = SaveSystem.getDoors();
            for (LockedDoor lockedDoor : doors) {
                if (lockedDoor.block.getLocation().equals(block.getLocation()) || lockedDoor.isNeighbor(block)) {
                    Door door = (Door)lockedDoor.block.getState().getData();
                    //Allows redstone to close a door but not open it
                    if (!door.isOpen() && lockedDoor.key != 0)
                        event.setNewCurrent(event.getOldCurrent());
                }
            }
        }
    }

    @Override
    public void onBlockBreak (BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        //Checks if broken Block is a Safe or a LockedDoor in the save file
        if (ChestLock.isSafe(block.getType())) {
            LinkedList<Safe> safes = SaveSystem.getSafes();
            for (Safe safe : safes) {
                if (safe.block.getLocation().equals(block.getLocation()) || safe.isNeighbor(block)) {
                    if (ChestLock.explosionProtection && player == null)
                        event.setCancelled(true);
                    else if (player.getName().equals(safe.owner) || ChestLock.hasPermission(player, "admin")) {
                        SaveSystem.removeSafe(safe);
                        return;
                    }
                    else
                        event.setCancelled(true);
                }
            }
        }
        else if (ChestLock.isDoor(block.getType())) {
            LinkedList<LockedDoor> doors = SaveSystem.getDoors();
            for (LockedDoor door : doors) {
                if (door.isNeighbor(block)) {
                    if (ChestLock.explosionProtection && player == null)
                        event.setCancelled(true);
                    else if (player.getName().equals(door.owner) || ChestLock.hasPermission(player, "admin")) {
                        SaveSystem.removeDoor(door);
                        return;
                    }
                    else
                        event.setCancelled(true);
                }
            }
        }
    }
}
