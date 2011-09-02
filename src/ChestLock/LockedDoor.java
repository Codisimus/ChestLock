
package ChestLock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;

/**
 * A LockedDoor is a Block that requires a key to open
 * 
 * @author Codisimus
 */
class LockedDoor {
    protected Block block;
    protected String owner;
    protected int key;

    /**
     * Constructs a new LockedDoor
     * 
     * @param owner The name of the owner of the door
     * @param block The Block of the door
     * @param key The item which must be in the players hand to open the door
     */
    public LockedDoor(String owner, Block block, int key) {
        this.owner = owner;
        this.block = block;
        this.key = key;
    }

    /**
     * Returns whether the given block is above or below the door Block
     * 
     * @param block The Block of the door
     * @return true if the given block is above or below the door Block
     */
    protected boolean isNeighbor(Block check) {
        int a = block.getX();
        int b = block.getY();
        int c = block.getZ();
        int x = check.getX();
        int y = check.getY();
        int z = check.getZ();
        if (block.getWorld() == check.getWorld())
            if (a == x && c == z)
                if (b == y+1 || b == y-1)
                    return true;
        return false;
    }

    /**
     * Returns true if the key is air or the player is holding the key
     * Returns true if the player has the admin permission and is holding the global key
     * 
     * @param player The player who may have the key
     * @return whether the player has the required key
     */
    public boolean hasKey(Player player) {
        if (key == 0)
            return true;
        else if (player.getItemInHand().getTypeId() == key)
            return true;
        else if (ChestLock.hasPermission(player, "admin")) {
            int global = Material.getMaterial(ChestLock.global).getId();
            if (player.getItemInHand().getTypeId() == global)
                return true;
        }
        return false;
    }
    
    /**
     * Opens/shuts the door if it is made of iron
     * 
     */
    protected void toggleOpen() {
        //Cancel if the door is unlockable
        if (key == 0)
            return;
        //Check for iron material
        if (isIron(block.getType())) {
            Door door = (Door)block.getState().getData();
            //Swing the door open/shut
            block.setData((byte)(block.getState().getData().getData()^4));
            Block neighbor;
            if (door.isTopHalf())
                neighbor = block.getRelative(BlockFace.DOWN);
            else
                neighbor = block.getRelative(BlockFace.UP);
            neighbor.setData((byte)(neighbor.getState().getData().getData()^4));
        }
    }
    
    /**
     * Returns true if the Material is iron
     * 
     * @param door The Material of the door block
     * @return true if the Material is iron
     */
    private boolean isIron(Material door) {
        if (door.equals(Material.IRON_DOOR))
            return true;
        else if (door.equals(Material.IRON_DOOR_BLOCK))
            return true;
        return false;
    }
}
