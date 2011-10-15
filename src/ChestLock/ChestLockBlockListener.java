
package ChestLock;

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
        if (!ChestLock.isDoor(block.getTypeId()))
            return;

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

        //Checks if broken Block is a Safe or a LockedDoor in the save file
        if (ChestLock.isSafe(block.getTypeId())) {
            Safe safe = SaveSystem.findSafe(block);
            if (safe == null)
                return;

            if (player != null && (player.getName().equals(safe.owner) || ChestLock.hasPermission(player, "admin"))) {
                SaveSystem.removeSafe(safe);
                return;
            }
            
            event.setCancelled(true);
        }
        else if (ChestLock.isDoor(block.getTypeId())) {
            LockedDoor door = SaveSystem.findDoor(block);
            if (door == null)
                return;

            if (player != null && (player.getName().equals(door.owner) || ChestLock.hasPermission(player, "admin"))) {
                SaveSystem.removeDoor(door);
                return;
            }
            
            event.setCancelled(true);
        }
    }
}
