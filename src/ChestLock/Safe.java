
package ChestLock;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * A Safe is a Chest, Furnace, or Dispenser which can be locked
 *
 * @author Cody
 */
class Safe {
    protected Block block;
    protected String owner;
    protected boolean locked = true;
    private String coOwners = ",";

    /**
     * Constructs a new Safe
     * 
     * @param owner The name of the owner of the safe
     * @param safe The Block of the safe
     * @param coOwners The list of the coowners of the safe
     */
    Safe(String owner, Block block, String coOwners) {
        this.owner = owner;
        this.block = block;
        this.coOwners = coOwners;
    }

    /**
     * Returns whether the given block is left or right of the safe Block
     * Will only return true is both block and safe are chests
     * 
     * @param block The Block of the safe
     * @return true if the given block is left or right of the safe Block
     */
    public boolean isNeighbor(Block safe) {
        if (block.getType().equals(Material.CHEST) && safe.getType().equals(Material.CHEST)) {
            int a = block.getX();
            int b = block.getY();
            int c = block.getZ();
            int x = safe.getX();
            int y = safe.getY();
            int z = safe.getZ();
            if (block.getWorld() == safe.getWorld())
                if (b == y)
                    if (a == x) {
                        if (c == z+1 || c == z-1)
                            return true;
                    }
                    else if (c == z)
                        if (a == x+1 || a == x-1)
                            return true;
        }
        return false;
    }

    /**
     * Returns whether the given player is a coowner
     * 
     * @param player The Player to be check for coownership
     * @return true if the given player is a coowner
     */
    public boolean isCoOwner(String player) {
        if (coOwners.contains(player))
            return true;
        return false;
    }

    /**
     * Returns the String of coowners
     * 
     * @return The String of coowners
     */
    public String getCoOwners() {
        return coOwners;
    }

    /**
     * Adds the name to the coowners
     * 
     * @param player The name of the player to be added
     */
    public void addCoOwner(String player) {
        coOwners = coOwners.concat(player+",");
    }

    /**
     * Removes the name from the coowners
     * 
     * @param player The name of the player to be removed
     */
    public boolean removeCoOwner(String player) {
        if (coOwners.contains(player)) {
            coOwners = coOwners.replace(player+",", "");
            return true;
        }
        return false;
    }
}
