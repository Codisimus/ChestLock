
package ChestLock;

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
import ru.tehkode.permissions.PermissionManager;

/**
 *
 * @author Cody
 */
public class ChestLock extends JavaPlugin {
    protected static Server server;
    protected static PermissionManager permissions;
    protected static PluginManager pm;
    protected static int own;
    protected static int lock;
    protected static int info;
    protected static int admin;
    protected static int disown;
    protected static int adminDisown;
    protected static int global;
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
        pm = server.getPluginManager();
        checkFiles();
        loadConfig();
        SaveSystem.load();
        registerEvents();
        System.out.println("ChestLock "+this.getDescription().getVersion()+" is enabled!");
    }
    
    /**
     * Makes sure all needed files exist
     *
     */
    private void checkFiles() {
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
    private void moveFile(String fileName) {
        try {
            JarFile jar = new JarFile("plugins/ChestLock.jar");
            ZipEntry entry = jar.getEntry(fileName);
            String destination = "plugins/ChestLock/";
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
        ChestLockPlayerListener.permission = format(loadValue("PermissionMessage"));
        ChestLockPlayerListener.lock = format(loadValue("LockMessage"));
        ChestLockPlayerListener.locked = format(loadValue("LockedMessage"));
        ChestLockPlayerListener.unlock = format(loadValue("UnlockMessage"));
        ChestLockPlayerListener.keySet = format(loadValue("KeySetMessage"));
        ChestLockPlayerListener.invalidKey = format(loadValue("InvalidKeyMessage"));
        ChestLockPlayerListener.unlockable = format(loadValue("UnlockableMessage"));
        ChestLockPlayerListener.lockable = format(loadValue("LockableMessage"));
        ChestLockPlayerListener.notown = format(loadValue("DoNotOwnMessage"));
        ChestLockPlayerListener.own = format(loadValue("OwnMessage"));
        ChestLockPlayerListener.disown = format(loadValue("DisownMessage"));
        ChestLockPlayerListener.limitMsg = format(loadValue("limitMessage"));
        ChestLockPlayerListener.clear = format(loadValue("ClearMessage"));
        Register.insufficientFunds = format(loadValue("InsufficientFundsMessage"));
    }

    /**
     * Converts the given String to the equal Material ID
     * 
     * @param type The given type (Material name or ID)
     * @return The Material ID
     */
    private int getID(String type) {
        if (type.equalsIgnoreCase("any"))
            return -1;
        
        Material mat = Material.getMaterial(type.toUpperCase());
        
        if (mat != null)
            return mat.getId();
        
        try {
            return Integer.parseInt(type);
        }
        catch (Exception notInt) {
            System.out.println(type+" is not a valid Block type");
            return -2;
        }
    }

    /**
     * Loads the given key and prints error if the key is missing
     *
     * @param key The key to be loaded
     * @return The String value of the loaded key
     */
    private String loadValue(String key) {
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
    private void registerEvents() {
        ChestLockBlockListener blockListener = new ChestLockBlockListener();
        ChestLockPlayerListener playerListener = new ChestLockPlayerListener();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, new PluginListener(), Priority.Monitor, this);
        pm.registerEvent(Type.WORLD_LOAD, new ChestLockWorldListener(), Priority.Normal, this);
        pm.registerEvent(Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
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
        else if (type.equals("admin") || type.equals("free"))
            return player.isOp();
        return true;
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
        if (permissions == null)
            return -1;

        if (hasPermission(player, "limit."+type+".-1"))
            return -1;

        for (int i = 100; i >= 0; i--)
            if (hasPermission(player, "limit."+type+"."+i))
                return i;
        return -1;
    }

    /**
     * Checks if the given Material ID is a Chest, Furnace, or Dispenser
     * Only returns true if the config settings say to lock that Block type
     *
     * @param id The Material ID to be checked
     * @return true if the Material is a Safe which can be locked
     */
    public static boolean isSafe(int id) {
        switch (id) {
            case 23: return lockDispensers; //Material == Dispenser
            case 54: return lockChests; //Material == Chest
            case 61: return lockFurnaces; //Material == Furnace
            case 62: return lockFurnaces; //Material == Burning Furnace
            default: return false;
        }
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
            case 64: return lockWoodDoors; //Material == Wooden Door
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
    private static String format(String string) {
        return string.replaceAll("&", "§").replaceAll("<ae>", "æ").replaceAll("<AE>", "Æ")
                .replaceAll("<o/>", "ø").replaceAll("<O/>", "Ø")
                .replaceAll("<a>", "å").replaceAll("<A>", "Å");
    }
}