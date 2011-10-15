
package ChestLock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author Cody
 */
class SaveSystem {
    private static LinkedList<Safe> safes = new LinkedList<Safe>();
    private static LinkedList<LockedDoor> doors = new LinkedList<LockedDoor>();
    private static boolean save = true;
    protected static boolean autoDelete;
    public static String saveType = "FlatFile";
    public static boolean convert = false;
    
    /**
     * Loads ChestLock data either from FlatFile or MySQL
     * MySQL is currently not working
     */
    protected static void load() {
        try {
            if (saveType.equalsIgnoreCase("MySQL"))
                if (convert)
                    loadFromFile();
                else
                    loadFromDB();
            else if (saveType.equalsIgnoreCase("FlatFile"))
                if (convert)
                    loadFromDB();
                else
                    loadFromFile();
            if (convert) {
                save();
                convert = false;
            }
        }
        catch (Exception e) {
            save = false;
            System.err.println("[ChestLock] Load failed, saving turned off to prevent loss of data");
            e.printStackTrace();
        }
    }
    
    /**
     * Reads save file to load ChestLock data
     * Saving is turned off (or line is deleted) if an error occurs
     */
    private static void loadFromFile() throws Exception {
        new File("plugins/ChestLock").mkdir();
        new File("plugins/ChestLock/chestlock.save").createNewFile();
        BufferedReader bReader = new BufferedReader(new FileReader("plugins/ChestLock/chestlock.save"));
        String line = "";
        while ((line = bReader.readLine()) != null) {
            String[] split = line.split(";");
            if (split[1].endsWith("~NETHER"))
                split[1].replace("~NETHER", "");
            World world = ChestLock.server.getWorld(split[1]);
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
                }
                else if (ChestLock.isSafe(id)) {
                    String coOwners = split[5];
                    if (coOwners.contains("unlockable"))
                            coOwners = "unlockable";
                    else {
                        if (coOwners.contains(",any,"))
                            coOwners = coOwners.replaceAll(",any,", ",");
                        if (!(coOwners.startsWith("player:") || coOwners.startsWith("group:"))) {
                            coOwners = coOwners.replaceAll(",", ",player:");
                            coOwners = coOwners.substring(0, coOwners.length()-7);
                        }
                        if (coOwners.startsWith(","))
                            coOwners = "CoOwners:"+coOwners;
                    }
                    Safe safe = new Safe(owner, block, coOwners);
                    safes.add(safe);
                }
                else {
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

    /**
     * Saves ChestLock data either to FlatFile or MySQL
     * MySQL is currently not working
     */
    public static void save() {
        //cancels if saving is turned off
        if (!save)
            return;
        try {
            if (saveType.equalsIgnoreCase("MySQL"))
                saveToDB();
            else if (saveType.equalsIgnoreCase("FlatFile"))
                saveToFile();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Reads save file to load ChestLock data for given World
     * Saving is turned off (or line is deleted) if an error occurs
     */
    protected static void loadData(World world) {
        try {
            new File("plugins/ChestLock").mkdir();
            new File("plugins/ChestLock/chestlock.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/ChestLock/chestlock.save"));
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                if (split[1].equals(world.getName())) {
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
                    }
                    else if (ChestLock.isSafe(id)) {
                        String coOwners = split[5];
                        if (coOwners.contains("unlockable"))
                            coOwners = "unlockable";
                        else {
                            if (coOwners.contains(",any,"))
                                coOwners = coOwners.replaceAll(",any,", ",");
                            if (!(coOwners.startsWith("player:") || coOwners.startsWith("group:"))) {
                                coOwners = coOwners.replaceAll(",", ",player:");
                                coOwners = coOwners.substring(0, coOwners.length()-7);
                            }
                            if (coOwners.startsWith(","))
                                coOwners = "CoOwners:"+coOwners;
                        }
                        Safe safe = new Safe(owner, block, coOwners);
                        safes.add(safe);
                    }
                    else {
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
        catch (Exception e) {
            save = false;
            System.err.println("[ChestLock] Load failed, saving turned off to prevent loss of data");
            e.printStackTrace();
        }
    }
    
    /**
     * Writes data to save file
     * Old file is overwritten
     */
    private static void saveToFile() throws Exception {
        BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/ChestLock/chestlock.save"));
        for(Safe safe : safes) {
            bWriter.write(safe.owner.concat(";"));
            Block chest = safe.block;
            bWriter.write(chest.getWorld().getName()+";");
            bWriter.write(chest.getX()+";");
            bWriter.write(chest.getY()+";");
            bWriter.write(chest.getZ()+";");
            bWriter.write(safe.coOwners+";");
            bWriter.newLine();
        }
        for(LockedDoor door : doors) {
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

    /**
     * Returns the LinkedList of saved Safes
     * 
     * @return The LinkedList of saved Safes
     */
    public static LinkedList<Safe> getSafes() {
        return safes;
    }
    
    /**
     * Returns the LinkedList of Safes owned by the given Player
     * 
     * @return The LinkedList of Safes owned by the given Player
     */
    public static LinkedList<Safe> getOwnedSafes(String player) {
        LinkedList<Safe> ownedSafes = new LinkedList<Safe>();
        for (Safe safe: safes)
            if (safe.owner.equals(player))
                ownedSafes.add(safe);
        return ownedSafes;
    }
    
    /**
     * Returns the Safe of given block
     * 
     * @return The Safe of given block
     */
    public static Safe findSafe(Block block) {
        for (Safe safe: safes)
            if (safe.block.getLocation().equals(block.getLocation()) || safe.isNeighbor(block))
                return safe;
        return null;
    }

    /**
     * Adds the Safe to the LinkedList of saved Safes
     * 
     * @param safe The Safe to be added
     */
    protected static void addSafe(Safe safe) {
        safes.add(safe);
    }

    /**
     * Removes the Safe from the LinkedList of saved Safes
     * 
     * @param safe The Safe to be removed
     */
    protected static void removeSafe(Safe safe) {
        safes.remove(safe);
    }
    
    /**
     * Returns the LinkedList of saved Doors
     * 
     * @return The LinkedList of saved Doors
     */
    public static LinkedList<LockedDoor> getDoors() {
        return doors;
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
     * Returns the LockedDoor of given block
     * 
     * @return The LockedDoor of given block
     */
    public static LockedDoor findDoor(Block block) {
        for (LockedDoor door: doors)
            if (door.block.getLocation().equals(block.getLocation()) || door.isNeighbor(block))
                return door;
        return null;
    }
    
    /**
     * Adds the LockedDoor to the LinkedList of saved Doors
     * 
     * @param door The LockedDoor to be added
     */
    protected static void addDoor(LockedDoor door) {
        doors.add(door);
    }

    /**
     * Removes the LockedDoor from the LinkedList of saved Doors
     * 
     * @param door The LockedDoor to be removed
     */
    protected static void removeDoor(LockedDoor door) {
        doors.remove(door);
    }
    
    /**
     * Removes the LockedDoors/Safes that are owned by the given player
     * 
     * @param player The name of the Player
     */
    protected static void clear(String player) {
        for (Safe safe: safes)
            if (safe.owner.equals(player))
                safes.remove(safe);
        for (LockedDoor door: doors)
            if (door.owner.equals(player))
                safes.remove(door);
        save();
    }

    /**
     * Loads ChestLock data from database
     * This is currently not working
     */
    private static void loadFromDB() throws Exception {
        Connection con = connectToDB();
        con.rollback();
    }
    
    /**
     * Saves ChestLock data to database
     * This is currently not working
     */
    private static void saveToDB() throws Exception {
        Connection con = connectToDB();
        con.setSavepoint();
    }
    
    /**
     * Connects to MySQL database
     * This is currently not working
     */
    private static Connection connectToDB() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/mysql";
            Connection con = DriverManager.getConnection(url, "root", "");
            System.out.println("Connected to db");
            return con;
        }
        catch(Exception e) {
            System.err.println("Could not connect to MySQL Database");
            e.printStackTrace();
            return null;
        }
    }
}
