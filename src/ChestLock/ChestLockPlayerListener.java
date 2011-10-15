
package ChestLock;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Cody
 */
public class ChestLockPlayerListener extends PlayerListener{
    public static String permission;
    public static String lock;
    public static String locked;
    public static String unlock;
    public static String keySet;
    public static String invalidKey;
    public static String unlockable;
    public static String lockable;
    public static String notown;
    public static String own;
    public static String disown;
    public static String limitMsg;
    public static String clear;
    public static double ownPrice;
    public static double lockPrice;

    @Override
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        if (split[0].equals("/lock")) {
            event.setCancelled(true);
            try {
                Block block = player.getTargetBlock(null, 100);
                if (split.length > 1) {
                    if (split[1].equals("help"))
                        throw new Exception();
                    else if (split[1].equals("list")) {
                        if (!ChestLock.hasPermission(player, "list."+split[2])) {
                            player.sendMessage(permission);
                            return;
                        }
                        if (split[2].equals("chests")) {
                            LinkedList<Safe> safes = SaveSystem.getOwnedSafes(player.getName());
                            player.sendMessage("Number owned: "+safes.size());
                            int limit = ChestLock.getOwnLimit(player, "chest");
                            if (limit > -1)
                                player.sendMessage("Total amount you may own: "+limit);
                            for (Safe safe: safes)
                                player.sendMessage(safe.block.getType().name()+" @"+getLocation(safe.block));
                        }
                        else if (split[2].equals("doors")) {
                            LinkedList<LockedDoor> doors = SaveSystem.getOwnedDoors(player.getName());
                            player.sendMessage("Number owned: "+doors.size());
                            int limit = ChestLock.getOwnLimit(player, "door");
                            if (limit > -1)
                                player.sendMessage("Total amount you may own: "+limit);
                            for (LockedDoor door: doors)
                                player.sendMessage("Key: "+Material.getMaterial(door.key).name()+" @"+getLocation(door.block));
                        }
                        else if (split[2].equals("owner")) {
                            if (ChestLock.isSafe(block.getTypeId())) {
                                Safe safe = SaveSystem.findSafe(block);
                                player.sendMessage("Owner: "+safe.owner);
                                for (String coOwner: safe.coOwners.split(","))
                                    player.sendMessage(coOwner);
                            }
                            if (ChestLock.isDoor(block.getTypeId())) {
                                LockedDoor door = SaveSystem.findDoor(block);
                                player.sendMessage("Owner: "+door.owner);
                            }
                        }
                        if (split[2].equals("clear")) {
                            SaveSystem.clear(player.getName());
                            player.sendMessage(clear);
                        }
                        else
                            return;
                    }
                    else if (split[1].equals("coowner")) {
                        if (!ChestLock.hasPermission(player, "coowner")) {
                            player.sendMessage(permission);
                            return;
                        }
                        Safe safe = SaveSystem.findSafe(block);
                        String type = block.getType().toString().toLowerCase();
                        if (!safe.owner.equalsIgnoreCase(player.getName())) {
                            String msg = notown.replaceAll("<blocktype>", type);
                            player.sendMessage(msg);
                            return;
                        }
                        if (safe.isUnlockable())
                            safe.coOwners = "";
                        String coOwner = split[2]+':'+split[3]+',';
                        if (safe.coOwners.contains(coOwner)) {
                            safe.coOwners = safe.coOwners.replace(coOwner, "");
                            player.sendMessage(split[3]+" was removed as coowner of "+type);
                        }
                        else {
                            safe.coOwners = safe.coOwners.concat(coOwner);
                            player.sendMessage(split[3]+" is now coowner of "+type);
                        }
                    }
                    else if (split[1].equals("never")) {
                        Safe safe = SaveSystem.findSafe(block);
                        String type = block.getType().toString().toLowerCase();
                        String msg;
                        if (!safe.owner.equalsIgnoreCase(player.getName())) {
                            msg = notown.replaceAll("<blocktype>", type);
                            player.sendMessage(msg);
                            return;
                        }
                        if (safe.isUnlockable()) {
                            safe.coOwners = "";
                            msg = lockable.replaceAll("<blocktype>", type);
                        }
                        else {
                            safe.coOwners = "unlockable";
                            msg = unlockable.replaceAll("<blocktype>", type);
                        }
                        player.sendMessage(msg);
                    }
                }
                else {
                    if (!ChestLock.hasPermission(player, "lock")) {
                        player.sendMessage(permission);
                        return;
                    }
                    if (ChestLock.isSafe(block.getTypeId())) {
                        Safe safe = SaveSystem.findSafe(block);
                        String type = block.getType().toString().toLowerCase();
                        if (!safe.owner.equalsIgnoreCase(player.getName())) {
                            player.sendMessage(notown.replaceAll("<blocktype>", type));
                            return;
                        }
                        if (safe.isUnlockable())
                            return;
                        if (!safe.locked && lockPrice > 0 && !ChestLock.hasPermission(player, "free"))
                            if (!Register.charge(player, lockPrice, type))
                                return;
                        safe.locked = !safe.locked;
                        String msg;
                        if (safe.locked)
                            msg = lock.replaceAll("<price>", ""+lockPrice).replaceAll("<blocktype>", type);
                        else
                            msg = unlock.replaceAll("<blocktype>", type);
                        player.sendMessage(msg);
                    }
                    else if (ChestLock.isDoor(block.getTypeId())) {
                        String type = "door";
                        String item = player.getItemInHand().getType().toString().toLowerCase();
                        LockedDoor door = SaveSystem.findDoor(block);
                        if (door == null) {
                            int limit = ChestLock.getOwnLimit(player, type);
                            if (limit > -1 && SaveSystem.getOwnedSafes(player.getName()).size() == limit) {
                                String msg = limitMsg.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
                                return;
                            }  
                            LockedDoor newDoor = new LockedDoor(player.getName(), block, player.getItemInHand().getTypeId());
                            SaveSystem.addDoor(newDoor);
                            String msg;
                            if (door.key == 0)
                                msg = unlockable.replaceAll("<blocktype>", type);
                            else
                                msg = keySet.replaceAll("<iteminhand>", item);
                            player.sendMessage(msg);
                        }
                        else
                            if (door.owner.equals(player.getName()) || ChestLock.hasPermission(player, "admin")) {
                                door.key = player.getItemInHand().getTypeId();
                                String msg;
                                if (door.key == 0)
                                    msg = unlockable.replaceAll("<blocktype>", type);
                                else
                                    msg = keySet.replaceAll("<iteminhand>", item);
                                player.sendMessage(msg);
                            }
                            else {
                                String msg = notown.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
                                return;
                            }
                    }
                }
                SaveSystem.save();
            }
            catch (Exception e) {
                player.sendMessage("§e     ChestLock Help Page:");
                player.sendMessage("§2/lock§b Lock/unlock target chest");
                player.sendMessage("§2/lock§b Set item in hand as key to target door");
                player.sendMessage("§2/lock never§b Make target chest unlockable");
                player.sendMessage("§2/lock (while holding nothing)§b Make target door unlockable");
                player.sendMessage("§2/lock coowner player [Name]§b Add Player as CoOwner of target");
                player.sendMessage("§2/lock coowner group [Name]§b Add Group as CoOwner of target");
                player.sendMessage("§2/lock list chests§b List all chests that you own");
                player.sendMessage("§2/lock list doors§b List all doors that you own");
                player.sendMessage("§2/lock list owner§b List the owner/CoOwners of target");
                player.sendMessage("§2/lock list clear§b Disown all chests and doors");
            }
        }
    }

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if Action was not clicking a Block
        if (!(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (ChestLock.isDoor(block.getTypeId())) {
            LinkedList<LockedDoor> Doors = SaveSystem.getDoors();
            for (LockedDoor door : Doors)
                if (door.block.getLocation().equals(block.getLocation()) || door.isNeighbor(block)) {
                    if (!ChestLock.hasPermission(player, "usekey")) {
                        player.sendMessage(permission);
                        return;
                    }
                    if (!door.hasKey(player)) {
                        event.setCancelled(true);
                        player.sendMessage(invalidKey);
                    }
                    else
                        door.toggleOpen();
                }
        }
        else if (ChestLock.isSafe(block.getTypeId()))  {
            String type = block.getType().toString().toLowerCase();
            if (type.equals("burning_furnace"))
                type = "furnace";
            LinkedList<Safe> Safes = SaveSystem.getSafes();
            for (Safe safe : Safes)
                if (safe.block.getLocation().equals(block.getLocation()) || safe.isNeighbor(block)) {
                    if (safe.isUnlockable())
                        return;
                    if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        if (safe.locked) {
                            event.setCancelled(true);
                            String msg = locked.replaceAll("<blocktype>", type);
                            player.sendMessage(msg);
                        }
                    }
                    else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                        if (safe.owner.equalsIgnoreCase(player.getName())) {
                            if (ChestLock.disown == -1 || ChestLock.disown == player.getItemInHand().getTypeId()) {
                                if (!ChestLock.hasPermission(player, "lock")) {
                                    player.sendMessage(permission);
                                    return;
                                }
                                SaveSystem.removeSafe(safe);
                                String msg = disown.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
                                SaveSystem.save();
                            }
                            else if (ChestLock.lock == -1 || ChestLock.lock == player.getItemInHand().getTypeId()) {
                                if (!ChestLock.hasPermission(player, "lock")) {
                                    player.sendMessage(permission);
                                    return;
                                }
                                if (!safe.locked && lockPrice > 0 && !ChestLock.hasPermission(player, "free"))
                                    if (!Register.charge(player, lockPrice, type))
                                        return;
                                safe.locked = !safe.locked;
                                String msg;
                                if (safe.locked)
                                    msg = lock.replaceAll("<price>", ""+lockPrice).replaceAll("<blocktype>", type);
                                else
                                    msg = unlock.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
                                SaveSystem.save();
                            }
                        }
                        else if (safe.isCoOwner(player)) {
                            if (ChestLock.lock == -1 || ChestLock.lock == player.getItemInHand().getTypeId()) {
                                if (!ChestLock.hasPermission(player, "lock")) {
                                    player.sendMessage(permission);
                                    return;
                                }
                                safe.locked = !safe.locked;
                                if (safe.locked) {
                                    String msg = lock.replaceAll("<blocktype>", type);
                                    player.sendMessage(msg);
                                }
                                else {
                                    String msg = unlock.replaceAll("<blocktype>", type);
                                    player.sendMessage(msg);
                                }
                                SaveSystem.save();
                            }
                        }
                        else if (ChestLock.hasPermission(player, "admin")) {
                            if (ChestLock.admin == -1 || ChestLock.admin == player.getItemInHand().getTypeId()) {
                                safe.locked = !safe.locked;
                                if (safe.locked) {
                                    String msg = lock.replaceAll("<blocktype>", type);
                                    player.sendMessage(msg);
                                }
                                else {
                                    String msg = unlock.replaceAll("<blocktype>", type);
                                    player.sendMessage(msg);
                                }
                                SaveSystem.save();
                            }
                            else if(ChestLock.adminDisown == -1 || ChestLock.adminDisown == player.getItemInHand().getTypeId()) {
                                SaveSystem.removeSafe(safe);
                                String msg = disown.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
                                SaveSystem.save();
                            }
                            else if (player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.info)))
                                player.sendMessage(type+" owned by: "+safe.owner);
                        }
                        else {
                            String msg = notown.replaceAll("<blocktype>", type);
                            player.sendMessage(msg);
                        }
                    return;
                }
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (!ChestLock.hasPermission(player, "lock")) {
                    player.sendMessage(permission);
                    return;
                }
                if (ChestLock.own == -1 || ChestLock.own == player.getItemInHand().getTypeId()) {
                    int limit = ChestLock.getOwnLimit(player, type);
                    if (limit > -1 && SaveSystem.getOwnedSafes(player.getName()).size() == limit) {
                        String msg = limitMsg.replaceAll("<blocktype>", type);
                        player.sendMessage(msg);
                        return;
                    }  
                    if (ownPrice > 0 && !ChestLock.hasPermission(player, "free"))
                        if (!Register.charge(player, ownPrice, type))
                            return;
                    SaveSystem.addSafe(new Safe(player.getName(), block, ","));
                    String msg = own.replaceAll("<price>", ""+ownPrice).replaceAll("<blocktype>", type);
                    player.sendMessage(msg);
                    SaveSystem.save();
                }
            }
        }
    }
    
    /**
     * Returns the location of a given block in the form of a string
     * 
     * @param The Block whose location will be returned
     * @return The location of a given block
     */
    private static String getLocation(Block block) {
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        return "World: "+world+" X: "+x+" Y: "+y+" Z: "+z;
    }
}
