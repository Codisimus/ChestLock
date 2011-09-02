
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
    protected static String permission;
    protected static String lock;
    protected static String locked;
    protected static String unlock;
    protected static String keySet;
    protected static String invalidKey;
    protected static String unlockable;
    protected static String lockable;
    protected static String notown;
    protected static String own;
    protected static String disown;
    protected static double ownPrice;
    protected static double lockPrice;

    @Override
    public void onPlayerCommandPreprocess (PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] split = event.getMessage().split(" ");
        if (split[0].equals("/coowner") || split[0].equals("/lock")) {
            event.setCancelled(true);
            try {
                if (split.length > 1 && split[1].equals("help"))
                    throw new Exception();
                Block block = player.getTargetBlock(null, 100);
                LinkedList<Safe> Safes = SaveSystem.getSafes();
                if (ChestLock.isDoor(block.getType())) {
                    String type = "door";
                    if (split[0].startsWith("/lock")) {
                        if (!ChestLock.hasPermission(player, "lock")) {
                            player.sendMessage(permission);
                            return;
                        }
                        LinkedList<LockedDoor> Doors = SaveSystem.getDoors();
                        String item = player.getItemInHand().getType().toString().toLowerCase();
                        for (LockedDoor door : Doors) {
                            if (door.block.getLocation().equals(block.getLocation()) || (door.isNeighbor(block))) {
                                if (door.owner.equals(player.getName()) || ChestLock.hasPermission(player, "admin")) {
                                    door.key = player.getItemInHand().getTypeId();
                                    if (door.key == 0) {
                                        String msg = unlockable.replaceAll("<blocktype>", type);
                                        player.sendMessage(msg);
                                    }
                                    else {
                                        String msg = keySet.replaceAll("<iteminhand>", item);
                                        player.sendMessage(msg);
                                    }
                                    SaveSystem.save();
                                }
                                else {
                                    String msg = notown.replaceAll("<blocktype>", type);
                                    player.sendMessage(msg);
                                }
                                return;
                            }
                        }
                        LockedDoor newDoor = new LockedDoor(player.getName(), block, player.getItemInHand().getTypeId());
                        SaveSystem.addDoor(newDoor);
                        String msg = keySet.replaceAll("<iteminhand>", item);
                        player.sendMessage(msg);
                        SaveSystem.save();
                    }
                }
                else if (ChestLock.isSafe(block.getType()))
                    for (Safe safe : Safes) {
                        if (safe.block.getLocation().equals(block.getLocation()) || safe.isNeighbor(block))
                            if (safe.owner.equalsIgnoreCase(player.getName())) {
                                String type = block.getType().toString().toLowerCase();
                                if (split[0].equals("/lock"))
                                    if (split.length > 1 && split[1].equals("never")) {
                                        if (!ChestLock.hasPermission(player, "unlockable")) {
                                            player.sendMessage(permission);
                                            return;
                                        }
                                        if (safe.isCoOwner("unlockable"))
                                            if (safe.removeCoOwner("unlockable")) {
                                                String msg = lockable.replaceAll("<blocktype>", type);
                                                player.sendMessage(msg);
                                                SaveSystem.save();
                                            }
                                            else
                                                player.sendMessage("Unexpected error, please try again");
                                        else {
                                            safe.addCoOwner("unlockable");
                                            String msg = unlockable.replaceAll("<blocktype>", type);
                                            player.sendMessage(msg);
                                            SaveSystem.save();
                                        }
                                    }
                                    else {
                                        if (!ChestLock.hasPermission(player, "lock")) {
                                            player.sendMessage(permission);
                                            return;
                                        }
                                        if (safe.isCoOwner("unlockable"))
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
                                        SaveSystem.save();
                                    }
                                else {
                                    if (split[0].equals("/coowner")) {
                                        if (!ChestLock.hasPermission(player, "coowner")) {
                                            player.sendMessage(permission);
                                            return;
                                        }
                                        if (safe.isCoOwner(split[1])) {
                                            if (safe.removeCoOwner(split[1])) {
                                                player.sendMessage(split[1]+" was removed as coowner of "+type);
                                                SaveSystem.save();
                                            }
                                            else
                                                player.sendMessage("Unexpected error, please try again");
                                        }
                                        else {
                                            safe.addCoOwner(split[1]);
                                            player.sendMessage(split[1]+" is now coowner of "+type);
                                            SaveSystem.save();
                                        }
                                    }
                                }
                            }
                    }
                else
                    throw new Exception();
            }
            catch (Exception e) {
                player.sendMessage("§e     ChestLock Help Page:");
                player.sendMessage("§2/coowner [Name]§b Add [Name] as CoOwner of target chest");
                player.sendMessage("§2/coowner any§b Allow anyone to lock/unlock the target chest");
                player.sendMessage("§2/lock§b Lock/unlock target chest");
                player.sendMessage("§2/lock§b Set item in hand as key to target door");
                player.sendMessage("§2/lock (while holding nothing)§b Make target door unlockable");
            }
        }
        else
            return;
    }

    @Override
    public void onPlayerInteract (PlayerInteractEvent event) {
        //Return if Action was not clicking a Block
        if (!(event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        if (ChestLock.isDoor(block.getType())) {
            LinkedList<LockedDoor> Doors = SaveSystem.getDoors();
            for (LockedDoor door : Doors) {
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
        }
        else if (ChestLock.isSafe(block.getType()))  {
            String type = block.getType().toString().toLowerCase();
            if (type.equals("burning_furnace"))
                type = "furnace";
            LinkedList<Safe> Safes = SaveSystem.getSafes();
            for (Safe safe : Safes) {
                if (safe.block.getLocation().equals(block.getLocation()) || safe.isNeighbor(block)) {
                    if (safe.isCoOwner("unlockable"))
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
                            if (ChestLock.disown.equalsIgnoreCase("any") ||
                                    player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.disown))) {
                                if (!ChestLock.hasPermission(player, "own")) {
                                    player.sendMessage(permission);
                                    return;
                                }
                                SaveSystem.removeSafe(safe);
                                String msg = disown.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
                            }
                            else if (ChestLock.lock.equalsIgnoreCase("any") ||
                                    player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.lock))) {
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
                        else if (safe.isCoOwner(player.getName()) || safe.isCoOwner("any")) {
                            if (ChestLock.lock.equalsIgnoreCase("any") ||
                                    player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.lock))) {
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
                            if (ChestLock.admin.equalsIgnoreCase("any") ||
                                    player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.admin))) {
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
                            else if(ChestLock.adminDisown.equalsIgnoreCase("any") ||
                                    player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.adminDisown))) {
                                SaveSystem.removeSafe(safe);
                                String msg = disown.replaceAll("<blocktype>", type);
                                player.sendMessage(msg);
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
            }
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (!ChestLock.hasPermission(player, "own")) {
                    player.sendMessage(permission);
                    return;
                }
                if (ChestLock.own.equalsIgnoreCase("any") ||
                        player.getItemInHand().getType().equals(Material.getMaterial(ChestLock.own))) {
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
}
