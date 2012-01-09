package com.codisimus.plugins.chestlock;

import com.codisimus.plugins.chestlock.listeners.WorldLoadListener;
import com.codisimus.plugins.chestlock.listeners.PlayerEventListener;
import com.codisimus.plugins.chestlock.listeners.BlockEventListener;
import com.codisimus.plugins.chestlock.listeners.CmmandListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Data/Permissions
 * 
 * @author Codisimus
 */
public class ChestLock extends JavaPlugin {
    private static Server server;
    static Permission permission;
    private static PluginManager pm;
    public static int own;
    public static int lock;
    public static int info;
    public static int admin;
    public static int disown;
    public static int adminDisown;
    public static int global;
    public static boolean explosionProtection;
    private Properties p;
    private static LinkedList<Safe> chests = new LinkedList<Safe>();
    private static LinkedList<Safe> furnaces = new LinkedList<Safe>();
    private static LinkedList<Safe> dispensers = new LinkedList<Safe>();
    public static LinkedList<LockedDoor> doors = new LinkedList<LockedDoor>();
    private static boolean save = true;
    private static boolean autoDelete;

    @Override
    public void onDisable () {
    }

    /**
     * Calls methods to load this Plugin when it is enabled
     *
     */
    @Override
    public void onEnable () {
        server = getServer();
        pm = server.getPluginManager();
        
        //Load Config settings
        loadSettings();
        
        for (World loadedWorld: server.getWorlds())
            loadData(loadedWorld);
        
        //Find Permissions
        RegisteredServiceProvider<Permission> permissionProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
            permission = permissionProvider.getProvider();
        
        //Find Economy
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
            Econ.economy = economyProvider.getProvider();
        
        //Read the command
        String commands = this.getDescription().getCommands().toString();
        CmmandListener.command = commands.substring(1, commands.indexOf("="));
        
        //Register Events
        BlockEventListener blockListener = new BlockEventListener();
        PlayerEventListener playerListener = new PlayerEventListener();
        pm.registerEvent(Type.WORLD_LOAD, new WorldLoadListener(), Priority.Monitor, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.High, this);
        getCommand(CmmandListener.command).setExecutor(new CmmandListener());
        
        System.out.println("ChestLock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Moves file from ChestLock.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/ChestLock.jar");
            ZipEntry entry = jar.getEntry(fileName);
            
            //Create the destination folder if it does not exist
            String destination = "plugins/ChestLock/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            
            //Copy the file
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0)
                    break;
                out.write(buffer, 0, nBytes);
            }
            
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception moveFailed) {
            System.err.println("[ChestLock] File Move Failed!");
            moveFailed.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    public void loadSettings() {
        p = new Properties();
        try {
            //Copy the file from the jar if it is missing
            if (!new File("plugins/ChestLock/config.properties").exists())
                moveFile("config.properties");
            
            FileInputStream fis = new FileInputStream("plugins/ChestLock/config.properties");
            p.load(fis);
            
            autoDelete = Boolean.parseBoolean(loadValue("AutoDelete"));
            explosionProtection = Boolean.parseBoolean(loadValue("ExplosionProtection"));
            PlayerEventListener.unlockToOpen = Boolean.parseBoolean(loadValue("UnlockToOpen"));
            
            PlayerEventListener.ownPrice = Integer.parseInt(loadValue("CostToOwn"));
            PlayerEventListener.lockPrice = Integer.parseInt(loadValue("CostToLock"));
            
            own = getID(loadValue("OwnTool"));
            lock = getID(loadValue("LockTool"));
            info = getID(loadValue("AdminInfoTool"));
            admin = getID(loadValue("AdminLockTool"));
            adminDisown = getID(loadValue("AdminDisownTool"));
            global = getID(loadValue("AdminGlobalKey"));
            disown = getID(loadValue("DisownTool"));
            
            PlayerEventListener.permissionMsg = format(loadValue("PermissionMessage"));
            PlayerEventListener.lockMsg = format(loadValue("LockMessage"));
            PlayerEventListener.lockedMsg = format(loadValue("LockedMessage"));
            PlayerEventListener.unlockMsg = format(loadValue("UnlockMessage"));
            CmmandListener.keySetMsg = format(loadValue("KeySetMessage"));
            PlayerEventListener.invalidKeyMsg = format(loadValue("InvalidKeyMessage"));
            CmmandListener.unlockableMsg = format(loadValue("UnlockableMessage"));
            CmmandListener.lockableMsg = format(loadValue("LockableMessage"));
            PlayerEventListener.doNotOwnMsg = format(loadValue("DoNotOwnMessage"));
            PlayerEventListener.ownMsg = format(loadValue("OwnMessage"));
            PlayerEventListener.disownMsg = format(loadValue("DisownMessage"));
            CmmandListener.limitMsg = format(loadValue("limitMessage"));
            CmmandListener.clearMsg = format(loadValue("ClearMessage"));
            Econ.insufficientFunds = format(loadValue("InsufficientFundsMessage"));
            
            fis.close();
        }
        catch (Exception missingProp) {
            System.err.println("Failed to load ChestLock "+this.getDescription().getVersion());
            missingProp.printStackTrace();
        }
    }

    /**
     * Converts the given String to the equal Material ID
     * 
     * @param type The given type (Material name or ID)
     * @return The Material ID
     */
    private int getID(String type) {
        //Return -1 for any Block type
        if (type.equalsIgnoreCase("any"))
            return -1;
        
        //Try to find the Material
        Material mat = Material.getMaterial(type.toUpperCase());
        if (mat != null)
            //Return the TypeID
            return mat.getId();
        
        //Iterate through the String to make sure each Character is a digit
        for (char c: type.toCharArray())
            if (!Character.isDigit(c)) {
                System.err.println("[ChestLock] "+type+" is not a valid Block type");
                return -2;
            }
        
        //Return the String as an int
        return Integer.parseInt(type);
    }

    /**
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private String loadValue(String key) {
        //Print an error if the key is not found
        if (!p.containsKey(key)) {
            System.err.println("[ChestLock] Missing value for "+key+" in config file");
            System.err.println("[ChestLock] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }

    /**
     * Returns boolean value of whether the given player has the specific permission
     * 
     * @param player The Player who is being checked for permission
     * @param node The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String node) {
        return permission.has(player, "chestlock."+node);
    }
    
    /**
     * Returns the Integer value that is the limit of items the given Player can own
     * Returns -1 if there is no limit
     * 
     * @param player The Player who is being checked for a limit
     * @param type The type of item (ex. chest or door)
     * @return The Integer value that is the limit of items the given Player can own
     */
    public static int getOwnLimit(Player player, String type) {
        //First check for no limit node
        if (hasPermission(player, "limit."+type+".-1"))
            return -1;

        //Start by checking a limit of 100 and work down to a limit of 0
        for (int i = 100; i >= 0; i--)
            if (hasPermission(player, "limit."+type+"."+i))
                return i;
        
        //No limit if a limit node was not found
        return -1;
    }
    
    /**
     * Adds various Unicode characters and colors to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    private static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
    
    /**
     * Reads save file to load ChestLock data
     * Loads only data for specific World if one is provided
     * Saving is turned off (or line is deleted) if an error occurs
     * 
     * @param world The World if one is provided
     */
    public static void loadData(World world) {
        try {
            //Open save file in BufferedReader
            new File("plugins/ChestLock").mkdir();
            new File("plugins/ChestLock/chestlock.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/ChestLock/chestlock.save"));

            //Convert each line into data until all lines are read
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                
                if (split[1].endsWith("~NETHER"))
                    split[1].replace("~NETHER", "");
                
                if (world.getName().equals(split[1])) {
                    String owner = split[0];
                    
                    int x = Integer.parseInt(split[2]);
                    int y = Integer.parseInt(split[3]);
                    int z = Integer.parseInt(split[4]);
                    Block block = world.getBlockAt(x, y, z);
                    
                    //Create a LockedDoor if the Block is a door
                    Material material = block.getType();
                    switch (material) {
                        case WOOD_DOOR: //Fall through
                        case WOODEN_DOOR: //Fall through
                        case IRON_DOOR: //Fall through
                        case IRON_DOOR_BLOCK:
                            doors.add(new LockedDoor(owner, block, Integer.parseInt(split[5])));
                            return;
                            
                        default: break;
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
                        coOwners.addAll(Arrays.asList(split[6].substring(1,
                                split[6].length() - 1).split(", ")));
                        groups.addAll(Arrays.asList(split[7].substring(1,
                                split[7].length() - 1).split(", ")));
                    }
                    
                    switch (material) {
                        case DISPENSER: dispensers.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        case CHEST: chests.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        case FURNACE: furnaces.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        case BURNING_FURNACE: furnaces.add(new Safe(owner, block, lockable, coOwners, groups)); break;
                        
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

            bReader.close();
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
        //Cancel if saving is turned off
        if (!save) {
            System.out.println("[ChestLock] Warning! Data is not being saved.");
            return;
        }
        
        try {
            //Open save file for writing data
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/ChestLock/chestlock.save"));
            
            //Write all Chest data to file
            for(Safe safe: chests) {
                //Write data in format "owner;x;y;z;lockable;coOwner1,coOwner2,...;group1,group2,...;
                bWriter.write(safe.owner.concat(";"));
                
                Block chest = safe.block;
                bWriter.write(chest.getWorld().getName().concat(";"));
                bWriter.write(chest.getX()+";");
                bWriter.write(chest.getY()+";");
                bWriter.write(chest.getZ()+";");
                
                bWriter.write(safe.lockable+";");
                
                bWriter.write(safe.coOwners.toString().concat(";"));
                bWriter.write(safe.groups.toString().concat(";"));
                
                //Write each Chest on its own line
                bWriter.newLine();
            }
            
            //First write all Furnace data to file
            for(Safe safe: furnaces) {
                //Write data in format "owner;x;y;z;lockable;coOwner1,coOwner2,...;group1,group2,...;
                bWriter.write(safe.owner.concat(";"));
                
                Block furnace = safe.block;
                bWriter.write(furnace.getWorld().getName().concat(";"));
                bWriter.write(furnace.getX()+";");
                bWriter.write(furnace.getY()+";");
                bWriter.write(furnace.getZ()+";");
                
                bWriter.write(safe.lockable+";");
                
                bWriter.write(safe.coOwners.toString().concat(";"));
                bWriter.write(safe.groups.toString().concat(";"));
                
                //Write each Furnace on its own line
                bWriter.newLine();
            }
            
            //First write all Dispenser data to file
            for(Safe safe: dispensers) {
                //Write data in format "owner;x;y;z;lockable;coOwner1,coOwner2,...;group1,group2,...;
                bWriter.write(safe.owner.concat(";"));
                
                Block dispenser = safe.block;
                bWriter.write(dispenser.getWorld().getName().concat(";"));
                bWriter.write(dispenser.getX()+";");
                bWriter.write(dispenser.getY()+";");
                bWriter.write(dispenser.getZ()+";");
                
                bWriter.write(safe.lockable+";");
                
                bWriter.write(safe.coOwners.toString().concat(";"));
                bWriter.write(safe.groups.toString().concat(";"));
                
                //Write each Dispenser on its own line
                bWriter.newLine();
            }
            
            //First write all Door data to file
            for(LockedDoor door: doors) {
                //Write data in format "owner;x;y;z;key;
                bWriter.write(door.owner.concat(";"));
                Block block = door.block;
                bWriter.write(block.getWorld().getName()+";");
                bWriter.write(block.getX()+";");
                bWriter.write(block.getY()+";");
                bWriter.write(block.getZ()+";");
                bWriter.write(door.key+";");
                
                //Write each Door on its own line
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
        
        //Iterate through all Chests and add the ones that the Player owns
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
        
        //Iterate through all Furnaces and add the ones that the Player owns
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
        
        //Iterate through all Dispensers and add the ones that the Player owns
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
        switch (block.getType()) {
            case DISPENSER:
                //Iterate through all Dispensers to find the one for the Block
                for (Safe safe: dispensers)
                    if (safe.block.equals(block))
                        return safe;
                
                //Return null because the block is not owned
                return null;
                
            case CHEST:
                //Iterate through all Chests to find the one for the Block
                for (Safe safe: chests)
                    if (safe.block.equals(block) || safe.isNeighbor(block))
                        return safe;
                
                //Return null because the block is not owned
                return null;
                
            case FURNACE:
                //Iterate through all Furnaces to find the one for the Block
                for (Safe safe: furnaces)
                    if (safe.block.equals(block))
                        return safe;
                
                //Return null because the block is not owned
                return null;
                
            case BURNING_FURNACE:
                //Iterate through all Furnaces to find the one for the Block
                for (Safe safe: furnaces)
                    if (safe.block.equals(block))
                        return safe;
                
                //Return null because the block is not owned
                return null;
                
            default: return null;
        }
    }
    
    /**
     * Adds the given Safe to the saved data
     * 
     * @param safe The given Safe
     */
    public static void addSafe(Safe safe) {
        switch (safe.block.getType()) {
            case DISPENSER: dispensers.add(safe); break;
            case CHEST: chests.add(safe); break;
            case FURNACE: furnaces.add(safe); break;
            case BURNING_FURNACE: furnaces.add(safe); break;
            default: break;
        }
    }
    
    /**
     * Removes the given Safe from the saved data
     * 
     * @param safe The given Safe
     */
    public static void removeSafe(Safe safe) {
        switch (safe.block.getType()) {
            case DISPENSER: dispensers.remove(safe); break;
            case CHEST: chests.remove(safe); break;
            case FURNACE: furnaces.remove(safe); break;
            case BURNING_FURNACE: furnaces.remove(safe); break;
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
        
        //Iterate through all Doors and add the ones that the Player owns
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
        //Return null is the Block is not a Door
        switch (block.getType()) {
            case WOOD_DOOR: break;
            case WOODEN_DOOR: break;
            case IRON_DOOR: break;
            case IRON_DOOR_BLOCK: break;
            default: return null;
        }
        
        //Iterate through all Doors to find the one for the Block
        for (LockedDoor door: doors)
            if (door.block.equals(block) || door.isNeighbor(block))
                return door;
        
        //Return null because the block is not owned
        return null;
    }
    
    /**
     * Removes the LockedDoors/Safes that are owned by the given Player
     * 
     * @param player The name of the Player
     */
    public static void clear(String player) {
        //Iterate through all Chests
        for (Safe safe: chests)
            //Remove the Chest if it is owned by the Player
            if (safe.owner.equals(player))
                chests.remove(safe);
        
        //Iterate through all Furnaces
        for (Safe safe: furnaces)
            //Remove the Furnace if it is owned by the Player
            if (safe.owner.equals(player))
                chests.remove(safe);
        
        //Iterate through all Dispensers
        for (Safe safe: dispensers)
            //Remove the Dispenser if it is owned by the Player
            if (safe.owner.equals(player))
                chests.remove(safe);
        
        //Iterate through all Doors
        for (LockedDoor door: doors)
            //Remove the Door if it is owned by the Player
            if (door.owner.equals(player))
                doors.remove(door);
        
        save();
    }
}