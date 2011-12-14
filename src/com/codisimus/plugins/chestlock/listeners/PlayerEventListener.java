package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.Econ;
import com.codisimus.plugins.chestlock.LockedDoor;
import com.codisimus.plugins.chestlock.Safe;
import com.codisimus.plugins.chestlock.SaveSystem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.material.Door;

/**
 * Checks for interactions with Safes and locked doors
 *
 * @author Codisimus
 */
public class PlayerEventListener extends PlayerListener {
    public static boolean unlockToOpen;
    public static String permissionMsg;
    public static String lockMsg;
    public static String lockedMsg;
    public static String unlockMsg;
    public static String invalidKeyMsg;
    public static String ownMsg;
    public static String disownMsg;
    public static String doNotOwnMsg;
    public static double ownPrice;
    public static double lockPrice;

    /**
     * Listens for Block interaction for owning Blocks or opening owned Blocks
     * 
     * @param event The PlayerInteractEvent that occurred
     */
    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if the Action was arm flailing
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        
        Player player = event.getPlayer();
        
        //Check if the Block is a LockedDoor
        LockedDoor lockedDoor = SaveSystem.findDoor(block);
        if (lockedDoor != null) {
            //Cancel if the Player does not have permission to open Locked Doors
            if (!ChestLock.hasPermission(player, "usekey")) {
                player.sendMessage(permissionMsg);
                return;
            }
            
            //Open/shut the Door only if the Player is holding the key
            if (lockedDoor.hasKey(player)) {
                switch (block.getType()) {
                    case IRON_DOOR: //Fall through
                    case IRON_DOOR_BLOCK:
                        //Convert the Block to a Door
                        BlockState state = block.getState();
                        Door door = (Door)state.getData();
                        
                        //Toggle the open status of the Door
                        door.setOpen(!door.isOpen());
                        state.update();
                        
                        //Get the other half of the Door
                        state = door.isTopHalf() ? block.getRelative(BlockFace.DOWN).getState()
                                : block.getRelative(BlockFace.UP).getState();
                        door = (Door)state.getData();
                        
                        //Toggle the open status of the Door
                        door.setOpen(!door.isOpen());
                        state.update();
                        
                        break;
                        
                    default: break;
                }
            }
            else {
                event.setCancelled(true);
                player.sendMessage(invalidKeyMsg);
            }
            
            return;
        }

        //Cancel if the Block is not a correct Safe type
        Material material = block.getType();
        switch (material) {
            case DISPENSER: break;
            case CHEST: break;
            case FURNACE: break;
            case BURNING_FURNACE: break;
            default: return;
        }
        
        //Get the Type of the Block
        String type = material.toString().toLowerCase();
        if (type.equals("burning_furnace"))
            type = "furnace";
        
        int holding = player.getItemInHand().getTypeId();
        Action action = event.getAction();
        
        Safe safe = SaveSystem.findSafe(block);
        if (safe == null) { //Safe is unowned
            //Return if the Player right clicked the Safe
            if (action.equals(Action.RIGHT_CLICK_BLOCK))
                return;
            
            //Return if the Player is not holding the 'own' tool
            if (ChestLock.own != -1 && ChestLock.own != holding)
                return;
            
            //Cancel if the Player does not have permission to use the command
            if (!ChestLock.hasPermission(player, "lock")) {
                player.sendMessage(permissionMsg);
                return;
            }
            
            int limit = ChestLock.getOwnLimit(player, type); 
            int owned = -2;
            
            //Retrieve number of owned Safes if there is a limit
            if (limit > -1)
                switch (material) {
                    case DISPENSER: owned = SaveSystem.getOwnedDispensers(player.getName()).size(); break;
                    case CHEST: owned = SaveSystem.getOwnedChests(player.getName()).size(); break;
                    case FURNACE: owned = SaveSystem.getOwnedFurnaces(player.getName()).size(); break;
                    case BURNING_FURNACE: owned = SaveSystem.getOwnedFurnaces(player.getName()).size(); break;
                    default: return;
                }
            
            //Return if the Player has reached their limit
            if (owned >= limit) {
                player.sendMessage(CommandListener.limitMsg);
                return;
            }
            
            //Charge the Player if they do not have the 'chestLock.free' node
            if (ownPrice > 0 && !ChestLock.hasPermission(player, "free"))
                //Return if the Player had insufficient funds
                if (!Econ.charge(player, ownPrice, type))
                    return;

            SaveSystem.addSafe(new Safe(player.getName(), block));

            String msg = ownMsg.replaceAll("<blocktype>", type);
            
            if (Econ.economy != null)
                msg = msg.replaceAll("<price>", ChestLock.hasPermission(player, "free")
                        ? Econ.format(0) : Econ.format(lockPrice));

            player.sendMessage(msg);
            SaveSystem.save();
            return;
        }

        //Return if the Safe is not lockable
        if (!safe.lockable)
            return;

        //Check if the Action was opening a Safe
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            //Cancel the Event if the Safe is locked
            if (safe.locked && (unlockToOpen || !(safe.owner.equalsIgnoreCase(player.getName())
                    || safe.isCoOwner(player)))) {
                event.setCancelled(true);
                player.sendMessage(lockedMsg.replaceAll("<blocktype>", type));
            }

            return;
        }
        
        //Check if the Player is the Owner/CoOwner
        if (safe.owner.equalsIgnoreCase(player.getName()) || safe.isCoOwner(player)) {
            //Remove the Safe if the Owner is attempting to disown it
            if (safe.owner.equalsIgnoreCase(player.getName()) && ChestLock.disown == -1 || ChestLock.disown == holding) {
                SaveSystem.removeSafe(safe);
                player.sendMessage(disownMsg.replaceAll("<blocktype>", type));
                SaveSystem.save();
                return;
            }

            //Check if the Player is attempting to lock the safe
            if (ChestLock.lock == -1 || ChestLock.lock == holding) {
                //Cancel if the Player does not have permission to use the command
                if (!ChestLock.hasPermission(player, "lock")) {
                    player.sendMessage(permissionMsg);
                    return;
                }

                //Charge the Player if they do not have the 'chestlock.free' node
                if (!safe.locked && lockPrice > 0 && !ChestLock.hasPermission(player, "free"))
                    //Return if the Player had insufficient funds
                    if (!Econ.charge(player, lockPrice, type))
                        return;

                safe.locked = !safe.locked;

                if (!safe.locked)
                    player.sendMessage(unlockMsg.replaceAll("<blocktype>", type));
                else {
                    String msg = lockedMsg.replaceAll("<blocktype>", type);
            
                    if (Econ.economy != null)
                        msg = msg.replaceAll("<price>", ChestLock.hasPermission(player, "free")
                                ? Econ.format(0) : Econ.format(lockPrice));

                    player.sendMessage(msg);
                }
                
                return;
            }
        }

        //Check if the Player is an Admin
        if (ChestLock.hasPermission(player, "admin")) {
            //Check if the Player is attempting to lock the safe
            if (ChestLock.admin == -1 || ChestLock.admin == holding) {
                safe.locked = !safe.locked;

                if (safe.locked)
                    player.sendMessage(lockMsg.replaceAll("<blocktype>", type));
                else
                    player.sendMessage(unlockMsg.replaceAll("<blocktype>", type));

                return;
            }

            //Check if the Player is requesting info on the safe
            if (ChestLock.info == holding) {
                player.sendMessage(type+" owned by: "+safe.owner);
                return;
            }

            //Check if the Player is attempting to disown the safe
            if (ChestLock.adminDisown == -1 || ChestLock.adminDisown == holding) {
                SaveSystem.removeSafe(safe);
                player.sendMessage(disownMsg.replaceAll("<blocktype>", type));
                SaveSystem.save();
                return;
            }
        }
        
        player.sendMessage(doNotOwnMsg.replaceAll("<blocktype>", type));
    }
}
