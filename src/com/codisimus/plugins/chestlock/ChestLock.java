package com.codisimus.plugins.chestlock;

import com.codisimus.plugins.chestlock.listeners.worldListener;
import com.codisimus.plugins.chestlock.listeners.pluginListener;
import com.codisimus.plugins.chestlock.listeners.playerListener;
import com.codisimus.plugins.chestlock.listeners.blockListener;
import com.codisimus.plugins.chestlock.listeners.commandListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;

/**
 * Loads Plugin and manages Permissions
 * 
 * @author Codisimus
 */
public class ChestLock extends JavaPlugin {
    public static Server server;
    public static PermissionManager permissions;
    public static PluginManager pm;
    public static int own;
    public static int lock;
    public static int info;
    public static int admin;
    public static int disown;
    public static int adminDisown;
    public static int global;
    public static boolean lockChests;
    public static boolean lockFurnaces;
    public static boolean lockDispensers;
    public static boolean lockWoodDoors;
    public static boolean lockIronDoors;
    public static boolean explosionProtection;
    public Properties p;

    @Override
    public void onDisable () {
    }

    @Override
    public void onEnable () {
        server = getServer();
        pm = server.getPluginManager();
        checkFiles();
        loadConfig();
        SaveSystem.load(null);
        registerEvents();
        getCommand("lock").setExecutor(new commandListener());
        System.out.println("ChestLock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Makes sure all needed files exist
     *
     */
    public void checkFiles() {
        File file = new File("plugins/ChestLock/config.properties");
        if (!file.exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from ChestLock.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    public void moveFile(String fileName) {
        try {
            //Retrieve file from this plugin's .jar
            JarFile jar = new JarFile("plugins/ChestLock.jar");
            ZipEntry entry = jar.getEntry(fileName);
            
            //Create the destination folder if it does not exist
            String destination = "plugins/ChestLock/";
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
            
            //Copy the file
            File efile = new File(destination, fileName);
            InputStream in = new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
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
    public void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/ChestLock/config.properties"));
        }
        catch (Exception e) {
        }
        SaveSystem.autoDelete = Boolean.parseBoolean(loadValue("AutoDelete"));
        Register.economy = loadValue("Economy");
        pluginListener.useBP = Boolean.parseBoolean(loadValue("UseBukkitPermissions"));
        playerListener.ownPrice = Integer.parseInt(loadValue("CostToOwn"));
        playerListener.lockPrice = Integer.parseInt(loadValue("CostToLock"));
        own = getID(loadValue("OwnTool"));
        lock = getID(loadValue("LockTool"));
        info = getID(loadValue("AdminInfoTool"));
        admin = getID(loadValue("AdminLockTool"));
        adminDisown = getID(loadValue("AdminLockTool"));
        global = getID(loadValue("AdminGlobalKey"));
        disown = getID(loadValue("DisownTool"));
        lockChests = Boolean.parseBoolean(loadValue("LockableChests"));
        lockFurnaces = Boolean.parseBoolean(loadValue("LockableFurnaces"));
        lockDispensers = Boolean.parseBoolean(loadValue("LockableDispenser"));
        lockWoodDoors = Boolean.parseBoolean(loadValue("LockableWoodDoors"));
        lockIronDoors = Boolean.parseBoolean(loadValue("LockableIronDoors"));
        explosionProtection = Boolean.parseBoolean(loadValue("ExplosionProtection"));
        playerListener.permissionMsg = format(loadValue("PermissionMessage"));
        playerListener.lockMsg = format(loadValue("LockMessage"));
        playerListener.lockedMsg = format(loadValue("LockedMessage"));
        playerListener.unlockMsg = format(loadValue("UnlockMessage"));
        commandListener.keySetMsg = format(loadValue("KeySetMessage"));
        playerListener.invalidKeyMsg = format(loadValue("InvalidKeyMessage"));
        commandListener.unlockableMsg = format(loadValue("UnlockableMessage"));
        commandListener.lockableMsg = format(loadValue("LockableMessage"));
        playerListener.doNotOwnMsg = format(loadValue("DoNotOwnMessage"));
        playerListener.ownMsg = format(loadValue("OwnMessage"));
        playerListener.disownMsg = format(loadValue("DisownMessage"));
        commandListener.limitMsg = format(loadValue("limitMessage"));
        commandListener.clearMsg = format(loadValue("ClearMessage"));
        Register.insufficientFunds = format(loadValue("InsufficientFundsMessage"));
    }

    /**
     * Converts the given String to the equal Material ID
     * 
     * @param type The given type (Material name or ID)
     * @return The Material ID
     */
    public int getID(String type) {
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
     * Loads the given key and prints error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    public String loadValue(String key) {
        //Print error if key is not found
        if (!p.containsKey(key)) {
            System.err.println("[ChestLock] Missing value for "+key+" in config file");
            System.err.println("[ChestLock] Please regenerate config file");
        }
        
        return p.getProperty(key);
    }
    
    /**
     * Registers events for the ChestLock Plugin
     *
     */
    public void registerEvents() {
        blockListener blockListener = new blockListener();
        playerListener playerListener = new playerListener();
        pm.registerEvent(Type.PLUGIN_ENABLE, new pluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.WORLD_LOAD, new worldListener(), Priority.Normal, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
    }

    /**
     * Returns boolean value of whether the given player has the specific permission
     * 
     * @param player The Player who is being checked for permission
     * @param type The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        //Check if a Permission Plugin is present
        if (permissions != null)
            return permissions.has(player, "chestlock."+type);
        
        //Return Bukkit Permission value
        return player.hasPermission("chestlock."+type);
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
        //No limit if a permissions plugin is not present
        if (permissions == null)
            return -1;

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
     * Checks if the given Material ID is a Door
     * Only returns true if the config settings say to lock doors
     * 
     * @param id The Material ID to be checked
     * @return true if the Material is a Door which can be locked
     */
    public static boolean isDoor(int id) {
        switch (id) {
            case 64: return lockWoodDoors; //Material == Wood Door
            case 71: return lockIronDoors; //Material == Iron Door
            case 324: return lockWoodDoors; //Material == Wood Door
            case 330: return lockIronDoors; //Material == Iron Door
            default: return false;
        }
    }
    
    /**
     * Adds various Unicode characters to a string
     * 
     * @param string The string being formated
     * @return The formatted String
     */
    public static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}