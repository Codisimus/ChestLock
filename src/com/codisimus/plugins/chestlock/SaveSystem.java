package com.codisimus.plugins.chestlock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedList;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Holds ChestLock data and is used to load/save data
 *
 * @author Codisimus
 */
public class SaveSystem {
    public static LinkedList<Safe> chests = new LinkedList<Safe>();
    public static LinkedList<Safe> furnaces = new LinkedList<Safe>();
    public static LinkedList<Safe> dispensers = new LinkedList<Safe>();
    public static LinkedList<LockedDoor> doors = new LinkedList<LockedDoor>();
    public static boolean save = true;
    public static boolean autoDelete;
    
    
    /**
     * Reads save file to load ChestLock data
     * Loads only data for specific World if one is provided
     * Saving is turned off (or line is deleted) if an error occurs
     * 
     * @param world The World if one is provided
     */
    public static void load(World world) {
        try {
            new File("plugins/ChestLock").mkdir();
            new File("plugins/ChestLock/chestlock.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/ChestLock/chestlock.save"));
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                if (split[1].endsWith("~NETHER"))
                    split[1].replace("~NETHER", "");
                if (world == null)
                    world = ChestLock.server.getWorld(split[1]);
                if (world != null) {
                    String owner = split[0];
                    int x = Integer.parseInt(split[2]);
                    int y = Integer.parseInt(split[3]);
                    int z = Integer.parseInt(split[4]);
                    Block block = world.getBlockAt(x, y, z);
                    int id = block.getTypeId();
                    if (ChestLock.isDoor(id)) {
                        int key = Integer.parseInt(split[5]);
                        LockedDoor door = new LockedDoor(owner, block, key);
                        doors.add(door);
                        return;
                    }
                    
                    boolean lockable = true;
                    LinkedList<String> coOwners = new LinkedList<String>();
                    LinkedList<String> groups = new LinkedList<String>();
                    
                    if (split.length == 6) {
                        String coOwnerList = split[5];
                        if (coOwnerList.contains("unlockable"))
                            lockable = false;
                        else {
                            if (coOwnerList.startsWith("CoOwners:"))
                                coOwnerList = coOwnerList.substring(9);
                            else
                                coOwnerList = coOwnerList.replaceAll(",any,", ",");
                            LinkedList<String> tempList = (LinkedList<String>)Arrays.asList(coOwnerList.split(","));
                            for (String coOwner: tempList)
                                if (coOwner.startsWith("player:"))
                                    coOwners.add(coOwner.substring(7));
                                else if (coOwner.startsWith("group:"))
                                    coOwners.add(coOwner.substring(6));
                                else
                                    coOwners.add(coOwner);
                        }
                    }
                    else {
                        lockable = Boolean.parseBoolean(split[5]);
                        coOwners = (LinkedList<String>)Arrays.asList(split[6].split(","));
                        groups = (LinkedList<String>)Arrays.asList(split[7].split(","));
                    }
                    
                    switch (id) {
                        case 23: dispensers.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        case 54: chests.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        case 61: furnaces.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        case 62: furnaces.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        default:
                            System.err.println("[ChestLock] Invalid blocktype for "+line);
                            if (autoDelete)
                                System.err.println("[ChestLock] AutoDelete set to true, errored data deleted");
                            else {
                                save = false;
                                System.err.println("[ChestLock] Saving turned off to prevent loss of data");
                            }
                    }
                }
            }
        }
        catch (Exception loadFailed) {
            save = false;
            System.err.println("[ChestLock] Load failed, saving turned off to prevent loss of data");
            loadFailed.printStackTrace();
        }
    }
    
    /**
     * Writes data to save file
     * Old file is overwritten
     */
    public static void save() {
        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/ChestLock/chestlock.save"));
            for(Safe safe: chests) {
                bWriter.write(safe.owner.concat(";"));
                Block chest = safe.block;
                bWriter.write(chest.getWorld().getName().concat(";"));
                bWriter.write(chest.getX()+";");
                bWriter.write(chest.getY()+";");
                bWriter.write(chest.getZ()+";");
                bWriter.write(safe.lockable+";");
                
                if (safe.coOwners.isEmpty())
                    bWriter.write("none");
                else
                    for (String coOwner: safe.coOwners)
                        bWriter.write(coOwner.concat(","));
                bWriter.write(";");

                if (safe.groups.isEmpty())
                    bWriter.write("none");
                else
                    for (String group: safe.groups)
                        bWriter.write(group.concat(","));
                bWriter.write(";");
                
                bWriter.newLine();
            }
            for(Safe safe: furnaces) {
                bWriter.write(safe.owner.concat(";"));
                Block furnace = safe.block;
                bWriter.write(furnace.getWorld().getName().concat(";"));
                bWriter.write(furnace.getX()+";");
                bWriter.write(furnace.getY()+";");
                bWriter.write(furnace.getZ()+";");
                bWriter.write(safe.lockable+";");
                
                if (safe.coOwners.isEmpty())
                    bWriter.write("none");
                else
                    for (String coOwner: safe.coOwners)
                        bWriter.write(coOwner.concat(","));
                bWriter.write(";");

                if (safe.groups.isEmpty())
                    bWriter.write("none");
                else
                    for (String group: safe.groups)
                        bWriter.write(group.concat(","));
                bWriter.write(";");
                
                bWriter.newLine();
            }
            for(Safe safe: dispensers) {
                bWriter.write(safe.owner.concat(";"));
                Block dispenser = safe.block;
                bWriter.write(dispenser.getWorld().getName().concat(";"));
                bWriter.write(dispenser.getX()+";");
                bWriter.write(dispenser.getY()+";");
                bWriter.write(dispenser.getZ()+";");
                bWriter.write(safe.lockable+";");
                
                if (safe.coOwners.isEmpty())
                    bWriter.write("none");
                else
                    for (String coOwner: safe.coOwners)
                        bWriter.write(coOwner.concat(","));
                bWriter.write(";");

                if (safe.groups.isEmpty())
                    bWriter.write("none");
                else
                    for (String group: safe.groups)
                        bWriter.write(group.concat(","));
                bWriter.write(";");
                
                bWriter.newLine();
            }
            for(LockedDoor door: doors) {
                bWriter.write(door.owner.concat(";"));
                Block block = door.block;
                bWriter.write(block.getWorld().getName()+";");
                bWriter.write(block.getX()+";");
                bWriter.write(block.getY()+";");
                bWriter.write(block.getZ()+";");
                bWriter.write(door.key+";");
                bWriter.newLine();
            }
            bWriter.close();
        }
        catch (Exception saveFailed) {
            System.err.println("[ChestLock] Save failed!");
            saveFailed.printStackTrace();
        }
    }
    
    /**
     * Returns the LinkedList of Chests owned by the given Player
     * 
     * @param player The given Player
     * @return The LinkedList of Chests owned by the given Player
     */
    public static LinkedList<Safe> getOwnedChests(String player) {
        LinkedList<Safe> ownedChests = new LinkedList<Safe>();
        for (Safe safe: chests)
            if (safe.owner.equals(player))
                ownedChests.add(safe);
        return ownedChests;
    }
    
    /**
     * Returns the LinkedList of Furnaces owned by the given Player
     * 
     * @param player The given Player
     * @return The LinkedList of Furnaces owned by the given Player
     */
    public static LinkedList<Safe> getOwnedFurnaces(String player) {
        LinkedList<Safe> ownedChests = new LinkedList<Safe>();
        for (Safe safe: chests)
            if (safe.owner.equals(player))
                ownedChests.add(safe);
        return ownedChests;
    }
    
    /**
     * Returns the LinkedList of Dispensers owned by the given Player
     * 
     * @param player The given Player
     * @return The LinkedList of Dispensers owned by the given Player
     */
    public static LinkedList<Safe> getOwnedDispensers(String player) {
        LinkedList<Safe> ownedChests = new LinkedList<Safe>();
        for (Safe safe: chests)
            if (safe.owner.equals(player))
                ownedChests.add(safe);
        return ownedChests;
    }
    
    /**
     * Returns the Safe of given Block
     * 
     * @param block The given Block
     * @return The Safe of given Block
     */
    public static Safe findSafe(Block block) {
        switch (block.getTypeId()) {
            case 23:
                for (Safe safe: dispensers)
                    if (safe.block.equals(block))
                        return safe;
                break;
            case 54:
                for (Safe safe: chests)
                    if (safe.block.equals(block) || safe.isNeighbor(block))
                        return safe;
                break;
            case 61:
                for (Safe safe: furnaces)
                    if (safe.block.equals(block))
                        return safe;
                break;
            case 62:
                for (Safe safe: furnaces)
                    if (safe.block.equals(block))
                        return safe;
                break;
            default: return null;
        }
        return null;
    }
    
    /**
     * Adds the given Safe to the saved data
     * 
     * @param safe The given Safe
     */
    public static void addSafe(Safe safe) {
        switch (safe.block.getTypeId()) {
            case 23: dispensers.remove(safe); break;
            case 54: chests.remove(safe); break;
            case 61: furnaces.remove(safe); break;
            case 62: furnaces.remove(safe); break;
            default: break;
        }
    }
    
    /**
     * Removes the given Safe from the saved data
     * 
     * @param safe The given Safe
     */
    public static void removeSafe(Safe safe) {
        switch (safe.block.getTypeId()) {
            case 23: dispensers.remove(safe); break;
            case 54: chests.remove(safe); break;
            case 61: furnaces.remove(safe); break;
            case 62: furnaces.remove(safe); break;
            default: break;
        }
    }
    
    /**
     * Returns the LinkedList of LockedDoors owned by the given Player
     * 
     * @return The LinkedList of LockedDoors owned by the given Player
     */
    public static LinkedList<LockedDoor> getOwnedDoors(String player) {
        LinkedList<LockedDoor> ownedDoors = new LinkedList<LockedDoor>();
        for (LockedDoor door: doors)
            if (door.owner.equals(player))
                ownedDoors.add(door);
        return ownedDoors;
    }
    
    /**
     * Returns the LockedDoor of given Block
     * 
     * @param block The given Block
     * @return The LockedDoor of given Block
     */
    public static LockedDoor findDoor(Block block) {
        if (!ChestLock.isDoor(block.getTypeId()))
            return null;
        for (LockedDoor door: doors)
            if (door.block.getLocation().equals(block.getLocation()) || door.isNeighbor(block))
                return door;
        return null;
    }
    
    /**
     * Removes the LockedDoors/Safes that are owned by the given Player
     * 
     * @param player The name of the Player
     */
    public static void clear(String player) {
        for (Safe safe: chests)
            if (safe.owner.equals(player))
                chests.remove(safe);
        for (Safe safe: furnaces)
            if (safe.owner.equals(player))
                chests.remove(safe);
        for (Safe safe: dispensers)
            if (safe.owner.equals(player))
                chests.remove(safe);
        for (LockedDoor door: doors)
            if (door.owner.equals(player))
                doors.remove(door);
        save();
    }
}
