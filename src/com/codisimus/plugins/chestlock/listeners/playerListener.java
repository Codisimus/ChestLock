package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.LockedDoor;
import com.codisimus.plugins.chestlock.Register;
import com.codisimus.plugins.chestlock.Safe;
import com.codisimus.plugins.chestlock.SaveSystem;
import org.bukkit.block.Block;
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
public class playerListener extends PlayerListener {
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

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if Action was not clicking a Block
        if (!(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;
        
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        
        LockedDoor lockedDoor = SaveSystem.findDoor(block);
        if (lockedDoor != null) {
            if (!ChestLock.hasPermission(player, "usekey")) {
                player.sendMessage(permissionMsg);
                return;
            }
            
            int id = block.getTypeId();
            
            if (id == 71 || id == 330) //Material == Iron Door
                if (lockedDoor.hasKey(player)) {
                    Door ironDoor = (Door)block.getState().getData();
                    ironDoor.setOpen(!ironDoor.isOpen());
                }
                else
                    player.sendMessage(invalidKeyMsg);
            else if (id == 64 || id == 324) //Material == Wood Door
                if (!lockedDoor.hasKey(player)) {
                    event.setCancelled(true);
                    player.sendMessage(invalidKeyMsg);
                }
            
            return;
        }
        
        String type = block.getType().toString().toLowerCase();
        if (type.equals("burning_furnace"))
            type = "furnace";
        
        int holding = player.getItemInHand().getTypeId();
        
        Safe safe = SaveSystem.findSafe(block);
        if (safe == null) {
            if (!ChestLock.hasPermission(player, "lock")) {
                player.sendMessage(permissionMsg);
                return;
            }
            
            if (ChestLock.own != -1 && ChestLock.own != holding)
                return;
            
            int limit = ChestLock.getOwnLimit(player, type); 
            int owned = -2;
            
            if (limit > -1)
                switch (block.getTypeId()) {
                    case 23: owned = SaveSystem.getOwnedDispensers(player.getName()).size(); break;
                    case 54: owned = SaveSystem.getOwnedChests(player.getName()).size(); break;
                    case 61: owned = SaveSystem.getOwnedFurnaces(player.getName()).size(); break;
                    case 62: owned = SaveSystem.getOwnedFurnaces(player.getName()).size(); break;
                }
            
            if (owned >= limit) {
                player.sendMessage(commandListener.limitMsg);
                return;
            }
            
            if (ownPrice > 0 && !ChestLock.hasPermission(player, "free"))
                if (!Register.charge(player, ownPrice, type))
                    return;

            SaveSystem.addSafe(new Safe(player.getName(), block));

            if (ChestLock.hasPermission(player, "free"))
                player.sendMessage(ownMsg.replaceAll("<price>", ""+Register.format(0)).replaceAll("<blocktype>", type));
            else
                player.sendMessage(ownMsg.replaceAll("<price>", ""+Register.format(lockPrice)).replaceAll("<blocktype>", type));

            SaveSystem.save();
            return;
        }

        if (!safe.lockable)
            return;

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (safe.locked) {
                event.setCancelled(true);
                player.sendMessage(lockedMsg.replaceAll("<blocktype>", type));
            }

            return;
        }
        
        if (safe.owner.equalsIgnoreCase(player.getName()) || safe.isCoOwner(player)) {
            if (safe.owner.equalsIgnoreCase(player.getName()) && ChestLock.disown == -1 || ChestLock.disown == holding) {
                SaveSystem.removeSafe(safe);
                player.sendMessage(disownMsg.replaceAll("<blocktype>", type));
                SaveSystem.save();
                return;
            }

            if (ChestLock.lock == -1 || ChestLock.lock == holding) {
                if (!ChestLock.hasPermission(player, "lock")) {
                    player.sendMessage(permissionMsg);
                    return;
                }

                if (!safe.locked && lockPrice > 0 && !ChestLock.hasPermission(player, "free"))
                    if (!Register.charge(player, lockPrice, type))
                        return;

                safe.locked = !safe.locked;

                if (!safe.locked)
                    player.sendMessage(unlockMsg.replaceAll("<blocktype>", type));
                else if (ChestLock.hasPermission(player, "free"))
                    player.sendMessage(lockMsg.replaceAll("<price>", ""+Register.format(0)).replaceAll("<blocktype>", type));
                else
                    player.sendMessage(lockMsg.replaceAll("<price>", ""+Register.format(lockPrice)).replaceAll("<blocktype>", type));
            }
        }

        if (ChestLock.hasPermission(player, "admin")) {
            if (ChestLock.admin == -1 || ChestLock.admin == holding) {
                safe.locked = !safe.locked;

                if (safe.locked)
                    player.sendMessage(lockMsg.replaceAll("<blocktype>", type));
                else
                    player.sendMessage(unlockMsg.replaceAll("<blocktype>", type));

                return;
            }

            if (ChestLock.info == holding) {
                player.sendMessage(type+" owned by: "+safe.owner);
                return;
            }

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
