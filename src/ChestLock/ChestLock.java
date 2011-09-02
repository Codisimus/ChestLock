
package ChestLock;

import com.nijiko.permissions.PermissionHandler;
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
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Cody
 */
public class ChestLock extends JavaPlugin {
    protected static Server server;
    protected static PermissionHandler permissions;
    protected static PluginManager pm;
    protected static String own;
    protected static String lock;
    protected static String info;
    protected static String admin;
    protected static String disown;
    protected static String adminDisown;
    protected static String global;
    protected static boolean lockChests;
    protected static boolean lockFurnaces;
    protected static boolean lockDispensers;
    protected static boolean lockWoodDoors;
    protected static boolean lockIronDoors;
    protected static boolean explosionProtection;
    private Properties p;

    @Override
    public void onDisable () {
    }

    @Override
    public void onEnable () {
        server = getServer();
        checkFiles();
        loadConfig();
        SaveSystem.load();
        pm = server.getPluginManager();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new PluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, new ChestLockBlockListener(), Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, new ChestLockBlockListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, new ChestLockPlayerListener(), Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new ChestLockPlayerListener(), Priority.Normal, this);
        System.out.println("ChestLock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Makes sure all needed files exist
     * Register.jar is for economy support
     */
    private void checkFiles() {
        File file = new File("lib/Register.jar");
        if (!file.exists() || file.length() < 43000)
            moveFile("Register.jar");
        file = new File("plugins/ChestLock/config.properties");
        if (!file.exists())
            moveFile("config.properties");
    }
    
    /**
     * Moves file from ChestLock.jar to appropriate folder
     * Destination folder is created if it doesn't exist
     * 
     * @param fileName The name of the file to be moved
     */
    private void moveFile(String fileName) {
        try {
            JarFile jar = new JarFile("plugins/ChestLock.jar");
            ZipEntry entry = jar.getEntry(fileName);
            String destination = "plugins/ChestLock/";
            if (fileName.equals("Register.jar")) {
                System.out.println("[ChestLock] Moving Files... Please Reload Server");
                destination = "lib/";
            }
            File file = new File(destination.substring(0, destination.length()-1));
            if (!file.exists())
                file.mkdir();
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads settings from the config.properties file
     * 
     */
    private void loadConfig() {
        p = new Properties();
        try {
            p.load(new FileInputStream("plugins/ChestLock/config.properties"));
        }
        catch (Exception e) {
        }
        SaveSystem.autoDelete = Boolean.parseBoolean(loadValue("AutoDelete"));
        Register.economy = loadValue("Economy");
        PluginListener.useOP = Boolean.parseBoolean(loadValue("UseOP"));
        ChestLockPlayerListener.ownPrice = Integer.parseInt(loadValue("CostToOwn"));
        ChestLockPlayerListener.lockPrice = Integer.parseInt(loadValue("CostToLock"));
        own = loadValue("OwnTool").toUpperCase();
        lock = loadValue("LockTool").toUpperCase();
        info = loadValue("AdminInfoTool").toUpperCase();
        admin = loadValue("AdminLockTool").toUpperCase();
        adminDisown = loadValue("AdminLockTool").toUpperCase();
        global = loadValue("AdminGlobalKey").toUpperCase();
        disown = loadValue("DisownTool").toUpperCase();
        lockChests = Boolean.parseBoolean(loadValue("LockableChests"));
        lockFurnaces = Boolean.parseBoolean(loadValue("LockableFurnaces"));
        lockDispensers = Boolean.parseBoolean(loadValue("LockableDispenser"));
        lockWoodDoors = Boolean.parseBoolean(loadValue("LockableWoodDoors"));
        lockIronDoors = Boolean.parseBoolean(loadValue("LockableIronDoors"));
        explosionProtection = Boolean.parseBoolean(loadValue("ExplosionProtection"));
        ChestLockPlayerListener.permission = loadValue("PermissionMessage").replaceAll("&", "§");
        ChestLockPlayerListener.lock = loadValue("LockMessage").replaceAll("&", "§");
        ChestLockPlayerListener.locked = loadValue("LockedMessage").replaceAll("&", "§");
        ChestLockPlayerListener.unlock = loadValue("UnlockMessage").replaceAll("&", "§");
        ChestLockPlayerListener.keySet = loadValue("KeySetMessage").replaceAll("&", "§");
        ChestLockPlayerListener.invalidKey = loadValue("InvalidKeyMessage").replaceAll("&", "§");
        ChestLockPlayerListener.unlockable = loadValue("UnlockableMessage").replaceAll("&", "§");
        ChestLockPlayerListener.lockable = loadValue("LockableMessage").replaceAll("&", "§");
        ChestLockPlayerListener.notown = loadValue("DoNotOwnMessage").replaceAll("&", "§");
        ChestLockPlayerListener.own = loadValue("OwnMessage").replaceAll("&", "§");
        ChestLockPlayerListener.disown = loadValue("DisownMessage").replaceAll("&", "§");
        Register.insufficientFunds = loadValue("InsufficientFundsMessage").replaceAll("&", "§");
    }

    /**
     * Prints error for missing values
     * 
     */
    private String loadValue(String key) {
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
     * @param type The String of the permission, ex. admin
     * @return true if the given player has the specific permission
     */
    public static boolean hasPermission(Player player, String type) {
        if (permissions != null)
            return permissions.has(player, "chestlock."+type);
        else
            if (type.equals("admin") || type.equals("free"))
                return player.isOp();
            else
                return true;
    }

    /**
     * Checks if the given Material is a Chest, Furnace, or Dispenser
     * Only returns true if the config settings say to lock that Block type
     * 
     * @param target The Material to be checked
     * @return true if the Material is a Safe
     */
    public static boolean isSafe(Material target) {
        if (target.equals(Material.CHEST) && lockChests)
            return true;
        else if (target.equals(Material.BURNING_FURNACE) && lockFurnaces)
            return true;
        else if (target.equals(Material.FURNACE) && lockFurnaces)
            return true;
        else if (target.equals(Material.DISPENSER) && lockDispensers)
            return true;
        return false;
    }

    /**
     * Checks if the given Material is a Door
     * Only returns true if the config settings say to lock doors
     * 
     * @param target The Material to be checked
     * @return true if the Material is a Door
     */
    public static boolean isDoor(Material target) {
        if (target.equals(Material.WOOD_DOOR) && lockWoodDoors)
            return true;
        else if (target.equals(Material.WOODEN_DOOR) && lockWoodDoors)
            return true;
        else if (target.equals(Material.IRON_DOOR) && lockIronDoors)
            return true;
        else if (target.equals(Material.IRON_DOOR_BLOCK) && lockIronDoors)
            return true;
        return false;
    }
}
