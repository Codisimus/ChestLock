
package ChestLock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * A Safe is a Chest, Furnace, or Dispenser which can be locked
 *
 * @author Cody
 */
class Safe {
    protected Block block;
    protected String owner;
    protected boolean locked = true;
    protected String coOwners = "";

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
     * Coowner includes being in a group that has coownership
     * 
     * @param player The Player to be check for coownership
     * @return true if the given player is a coowner
     */
    public boolean isCoOwner(Player player) {
        if (coOwners.contains("player:"+player.getName()+","))
            return true;
        else {
            String[] split = coOwners.split(",");
            for (String coOwner: split)
                if (coOwner.startsWith("group:")) {
                    String group = coOwner.substring(6);
                    if (ChestLock.permissions.getUser(player).inGroup(group))
                        return true;
                }
        }
        return false;
    }
    
    /**
     * Returns whether the safe is unlockable
     * 
     * @return true if the safe is unlockable
     */
    public boolean isUnlockable() {
        if (coOwners.equals("unlockable"))
            return true;
        return false;
    }
}
