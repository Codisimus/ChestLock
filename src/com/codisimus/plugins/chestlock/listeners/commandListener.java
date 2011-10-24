package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.LockedDoor;
import com.codisimus.plugins.chestlock.Safe;
import com.codisimus.plugins.chestlock.SaveSystem;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Executes Player Commands
 * 
 * @author Codisimus
 */
public class commandListener implements CommandExecutor {
    //public static final HashSet TRANSPARENT = Sets.newHashSet(27, 28, 37, 38, 39, 40, 50, 65, 66, 69, 70, 72, 75, 76, 78);
    public static int cornerID;
    public static String clearMsg;
    public static String keySetMsg;
    public static String unlockableMsg;
    public static String lockableMsg;
    public static String limitMsg;
    
    /**
     * Listens for ChestLock commands to execute them
     * 
     * @param sender The CommandSender who may not be a Player
     * @param command The command that was executed
     * @param alias The alias that the sender used
     * @param args The arguments for the command
     * @return true always
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        //Cancel if the command is not from a Player
        if (!(sender instanceof Player))
            return true;
        
        Player player = (Player)sender;
        
        //Display help page if the Player did not add any arguments
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        //Set the ID of the command
        int commandID = 0;
        if (args[0].equals("list"))
            commandID = 1;
        else if (args[0].equals("coowner"))
            commandID = 2;
        else if (args[0].equals("never"))
            commandID = 3;
        
        //Execute the command
        switch (commandID) {
            case 1:
                if (args.length == 2)
                    list(player, args[1]);
                else
                    sendHelp(player);
                return true;
            case 2:
                if (args.length == 4)
                    coowner(player, args[2], args[1], args[3]);
                else
                    sendHelp(player);
                return true;
            case 3:
                if (args[1].equals("true") || args[1].equals("false"))
                    setLockable(player, Boolean.parseBoolean(args[1]));
                else
                    sendHelp(player);
                return true;
            default:
                if (args.length == 1)
                    lock(player);
                else
                    sendHelp(player);
                return true;
        }
    }
    
    public static void list(Player player, String type) {
        //Cancel if the Player does not have permission to use the command
        if (!ChestLock.hasPermission(player, "list."+type)) {
            player.sendMessage(playerListener.permissionMsg);
            return;
        }
        
        //Set the ID of the type
        int typeID = 0;
        if (type.equals("chests"))
            typeID = 1;
        else if (type.equals("furnaces"))
            typeID = 2;
        else if (type.equals("dispensers"))
            typeID = 3;
        else if (type.equals("doors"))
            typeID = 4;
        else if (type.equals("owner"))
            typeID = 5;
        else if (type.equals("clear"))
            typeID = 6;

        switch (typeID) {
            case 1:
                LinkedList<Safe> ownedChests = SaveSystem.getOwnedChests(player.getName());
                String chestMsg = "You own "+ownedChests.size()+" Chests";
                int chestLimit = ChestLock.getOwnLimit(player, "chest");
                if (chestLimit > -1)
                    chestMsg.concat("and you may own a total of "+chestLimit);
                player.sendMessage(chestMsg);
                chestMsg = "";
                for (Safe safe: ownedChests)
                    chestMsg.concat(", @"+getLocation(safe.block));
                if (!chestMsg.isEmpty())
                    player.sendMessage(chestMsg.substring(2));
                break;
            case 2:
                LinkedList<Safe> ownedFurnaces = SaveSystem.getOwnedFurnaces(player.getName());
                String furnaceMsg = "You own "+ownedFurnaces.size()+" Furnaces";
                int furnaceLimit = ChestLock.getOwnLimit(player, "furnace");
                if (furnaceLimit > -1)
                    furnaceMsg.concat("and you may own a total of "+furnaceLimit);
                player.sendMessage(furnaceMsg);
                furnaceMsg = "";
                for (Safe safe: ownedFurnaces)
                    furnaceMsg.concat(", @"+getLocation(safe.block));
                if (!furnaceMsg.isEmpty())
                    player.sendMessage(furnaceMsg.substring(2));
                break;
            case 3:
                LinkedList<Safe> ownedDispensers = SaveSystem.getOwnedDispensers(player.getName());
                String dispenerMsg = "You own "+ownedDispensers.size()+" Dispensers";
                int dispenerLimit = ChestLock.getOwnLimit(player, "dispener");
                if (dispenerLimit > -1)
                    dispenerMsg.concat("and you may own a total of "+dispenerLimit);
                player.sendMessage(dispenerMsg);
                dispenerMsg = "";
                for (Safe safe: ownedDispensers)
                    dispenerMsg.concat(", @"+getLocation(safe.block));
                if (!dispenerMsg.isEmpty())
                    player.sendMessage(dispenerMsg.substring(2));
                break;
            case 4:
                LinkedList<LockedDoor> ownedDoors = SaveSystem.getOwnedDoors(player.getName());
                String doorMsg = "You own "+ownedDoors.size()+" Doors";
                int doorLimit = ChestLock.getOwnLimit(player, "door");
                if (doorLimit > -1)
                    doorMsg.concat("and you may own a total of "+doorLimit);
                player.sendMessage(doorMsg);
                doorMsg = "";
                for (LockedDoor door: ownedDoors)
                    player.sendMessage("Key: "+Material.getMaterial(door.key).name()+" @"+getLocation(door.block));
                if (!doorMsg.isEmpty())
                    player.sendMessage(doorMsg.substring(2));
                break;
            case 5:
                Block block = player.getTargetBlock(null, 10);
                
                LockedDoor door = SaveSystem.findDoor(block);
                
                if (door != null)
                    player.sendMessage("Owner: "+door.owner);
                else {
                    Safe safe = SaveSystem.findSafe(block);
                    
                    player.sendMessage("Owner: "+safe.owner);
                    
                    //Display CoOwners of OwnedChunk to Player
                    String coOwners = "CoOwners:  ";
                    for (String coOwner: safe.coOwners)
                        coOwners.concat(coOwner.concat(", "));
                    player.sendMessage(coOwners.substring(0, coOwners.length() - 2));

                    //Display CoOwner Groups of OwnedChunk to Player
                    String groups = "CoOwner Groups:  ";
                    for (String group: safe.groups)
                        groups.concat(group.concat(", "));
                    player.sendMessage(groups.substring(0, groups.length() - 2));
                }
                break;
            case 6:
                SaveSystem.clear(player.getName());
                player.sendMessage(clearMsg);
                break;
            default: sendHelp(player); break;
        }
        
        return;
    }
    
    /**
     * Manages CoOwnership of the target Block if the Player is the Owner
     * 
     * @param player The given Player who may be the Owner
     * @param type The given type: 'player' or 'group'
     * @param action The given action: 'add' or 'remove'
     * @param coOwner The given CoOwner
     */
    public static void coowner(Player player, String type, String action, String coOwner) {
        //Cancel if the Player does not have permission to use the command
        if (!ChestLock.hasPermission(player, "coowner")) {
            player.sendMessage(playerListener.permissionMsg);
            return;
        }
        
        Block block = player.getTargetBlock(null, 10);
        Safe safe = SaveSystem.findSafe(block);
        
        if (!safe.owner.equals(player.getName())) {
            player.sendMessage(playerListener.doNotOwnMsg.replaceAll("<blocktype>", block.getType().toString().toLowerCase()));
            return;
        }

        //Determine the command to execute
        if (type.equals("player"))
            if (action.equals("add")) {
                //Cancel if the Player is already a CoOwner
                if (safe.coOwners.contains(coOwner)) {
                    player.sendMessage(coOwner+" is already a CoOwner");
                    return;
                }
                safe.coOwners.add(coOwner);
                player.sendMessage(coOwner+" added as a CoOwner");
            }
            else if (action.equals("remove"))
                safe.coOwners.remove(coOwner);
            else {
                sendHelp(player);
                return;
            }
        else if(type.equals("group"))
            if (action.equals("add")) {
                //Cancel if the Group is already a CoOwner
                if (safe.groups.contains(coOwner)) {
                    player.sendMessage(coOwner+" is already a CoOwner");
                    return;
                }
                safe.groups.add(coOwner);
                player.sendMessage(coOwner+" added as a CoOwner");
            }
            else if (action.equals("remove"))
                safe.groups.remove(coOwner);
            else {
                sendHelp(player);
                return;
            }
        else {
            sendHelp(player);
            return;
        }
        
        SaveSystem.save();
    }
    
    public static void setLockable(Player player, boolean bool) {
        Block block = player.getTargetBlock(null, 10);
        Safe safe = SaveSystem.findSafe(block);
        
        if (!safe.owner.equals(player.getName())) {
            player.sendMessage(playerListener.doNotOwnMsg.replaceAll("<blocktype>", block.getType().toString().toLowerCase()));
            return;
        }
        
        if (safe.lockable)
            if (bool)
                player.sendMessage(block.getType().toString().toLowerCase()+" is already lockable.");
            else {
                safe.lockable = false;
                player.sendMessage(block.getType().toString().toLowerCase()+" is now unlockable.");
            }
        else
            if (bool) {
                safe.lockable = true;
                player.sendMessage(block.getType().toString().toLowerCase()+" is now lockable.");
            }
            else
                player.sendMessage(block.getType().toString().toLowerCase()+" is already unlockable.");
        
        SaveSystem.save();
    }
    
    public static void lock(Player player) {
        //Cancel if the Player does not have permission to use the command
        if (!ChestLock.hasPermission(player, "lock")) {
            player.sendMessage(playerListener.permissionMsg);
            return;
        }
        
        Block block = player.getTargetBlock(null, 10);
        
        if (!ChestLock.isDoor(block.getTypeId())) {
            sendHelp(player);
            return;
        }
        
        ItemStack item = player.getItemInHand();
        int id = item.getTypeId();
        LockedDoor door = SaveSystem.findDoor(block);
        
        if (door == null) {
            int limit = ChestLock.getOwnLimit(player, "door");
            
            if (limit > -1 && SaveSystem.getOwnedDoors(player.getName()).size() >= limit) {
                player.sendMessage(limitMsg.replaceAll("<blocktype>", "door"));
                return;
            }  
            
            SaveSystem.doors.add(new LockedDoor(player.getName(), block, id));
        }
        else {
            if (!door.owner.equals(player.getName()) && !ChestLock.hasPermission(player, "admin")) {
                player.sendMessage(playerListener.doNotOwnMsg.replaceAll("<blocktype>", "door"));
                return;
            }
            
            door.key = id;
        }

        if (id == 0)
            player.sendMessage(unlockableMsg.replaceAll("<blocktype>", "door"));
        else
            player.sendMessage(keySetMsg.replaceAll("<iteminhand>", item.getType().toString().toLowerCase()));
        
        SaveSystem.save();
    }
    
    /**
     * Displays the ChestLock Help Page to the given Player
     *
     * @param player The Player needing help
     */
    public static void sendHelp(Player player) {
        player.sendMessage("§e     ChestLock Help Page:");
        player.sendMessage("§2/lock§b Set item in hand as key to target door");
        player.sendMessage("§2/lock ['true' or 'false']§b Set if target can be locked");
        player.sendMessage("§2/lock (while holding nothing)§b Make target door unlockable");
        player.sendMessage("§2/lock coowner player [Name]§b Add Player as CoOwner of target");
        player.sendMessage("§2/lock coowner group [Name]§b Add Group as CoOwner of target");
        player.sendMessage("§2/lock list [BlockType]§b List all Blocks you own of given type");
        player.sendMessage("§2/lock list owner§b List the owner/CoOwners of target");
        player.sendMessage("§2/lock list clear§b Disown all chests and doors");
    }

    /**
     * Returns the location of a given block in the form of a string
     * 
     * @param The Block whose location will be returned
     * @return The location of a given block
     */
    public static String getLocation(Block block) {
        return "World: "+block.getWorld().getName()+" X: "+block.getX()+" Y: "+block.getY()+" Z: "+block.getZ();
    }
}
