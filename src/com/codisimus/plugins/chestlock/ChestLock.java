package com.codisimus.plugins.chestlock;

import com.codisimus.plugins.chestlock.listeners.WorldLoadListener;
import com.codisimus.plugins.chestlock.listeners.PlayerEventListener;
import com.codisimus.plugins.chestlock.listeners.BlockEventListener;
import com.codisimus.plugins.chestlock.listeners.CommandListener;
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
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads Plugin and manages Permissions
 * 
 * @author Codisimus
 */
public class ChestLock extends JavaPlugin {
    public static Server server;
    public static Permission permission;
    public static PluginManager pm;
    public static int own;
    public static int lock;
    public static int info;
    public static int admin;
    public static int disown;
    public static int adminDisown;
    public static int global;
    public static boolean explosionProtection;
    public Properties p;

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
        loadConfig();
        
        for (World loadedWorld: server.getWorlds())
            SaveSystem.load(loadedWorld);
        
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
        
        //Register Events
        BlockEventListener blockListener = new BlockEventListener();
        PlayerEventListener playerListener = new PlayerEventListener();
        pm.registerEvent(Type.WORLD_LOAD, new WorldLoadListener(), Priority.Monitor, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.High, this);
        getCommand("lock").setExecutor(new CommandListener());
        
        System.out.println("ChestLock "+this.getDescription().getVersion()+" is enabled!");
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
            //Copy the file from the jar if it is missing
            if (!new File("plugins/ChestLock/config.properties").exists())
                moveFile("config.properties");
            
            p.load(new FileInputStream("plugins/ChestLock/config.properties"));
            
            SaveSystem.autoDelete = Boolean.parseBoolean(loadValue("AutoDelete"));
            explosionProtection = Boolean.parseBoolean(loadValue("ExplosionProtection"));
            PlayerEventListener.unlockToOpen = Boolean.parseBoolean(loadValue("UnlockToOpen"));
            
            PlayerEventListener.ownPrice = Integer.parseInt(loadValue("CostToOwn"));
            PlayerEventListener.lockPrice = Integer.parseInt(loadValue("CostToLock"));
            
            own = getID(loadValue("OwnTool"));
            lock = getID(loadValue("LockTool"));
            info = getID(loadValue("AdminInfoTool"));
            admin = getID(loadValue("AdminLockTool"));
            adminDisown = getID(loadValue("AdminLockTool"));
            global = getID(loadValue("AdminGlobalKey"));
            disown = getID(loadValue("DisownTool"));
            
            PlayerEventListener.permissionMsg = format(loadValue("PermissionMessage"));
            PlayerEventListener.lockMsg = format(loadValue("LockMessage"));
            PlayerEventListener.lockedMsg = format(loadValue("LockedMessage"));
            PlayerEventListener.unlockMsg = format(loadValue("UnlockMessage"));
            CommandListener.keySetMsg = format(loadValue("KeySetMessage"));
            PlayerEventListener.invalidKeyMsg = format(loadValue("InvalidKeyMessage"));
            CommandListener.unlockableMsg = format(loadValue("UnlockableMessage"));
            CommandListener.lockableMsg = format(loadValue("LockableMessage"));
            PlayerEventListener.doNotOwnMsg = format(loadValue("DoNotOwnMessage"));
            PlayerEventListener.ownMsg = format(loadValue("OwnMessage"));
            PlayerEventListener.disownMsg = format(loadValue("DisownMessage"));
            CommandListener.limitMsg = format(loadValue("limitMessage"));
            CommandListener.clearMsg = format(loadValue("ClearMessage"));
            Econ.insufficientFunds = format(loadValue("InsufficientFundsMessage"));
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
     * Loads the given key and prints an error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    public String loadValue(String key) {
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
    public static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}