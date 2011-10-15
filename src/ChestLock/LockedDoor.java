
package ChestLock;

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
        int a = block.getY();
        int b = check.getY();
        
        if (block.getWorld() != check.getWorld() || block.getX() != check.getX() || block.getZ() != check.getZ())
            return false;
        
        if (a == b+1 || a == b-1)
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
        //Return true if the door is unlockable
        if (key == 0)
            return true;
        
        int held = player.getItemInHand().getTypeId();
        
        //Return true if the Player is holding the key
        if (held == key)
            return true;
        
        return ChestLock.hasPermission(player, "admin") && player.getItemInHand().getTypeId() == ChestLock.global;
    }
    
    /**
     * Opens/shuts the door if it is made of iron
     * 
     */
    protected void toggleOpen() {
        //Cancel if the door is unlockable
        if (key == 0)
            return;

        //Trigger swing open if Iron Door
        if (isIron(block.getTypeId())) {
            Door door = (Door)block.getState().getData();

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
     * Checks if the given Material ID is an Iron Door
     *
     * @param id The Material ID to be checked
     * @return true if the Material is an Iron Door
     */
    public static boolean isIron(int id) {
        switch (id) {
            case 71: return true;
            case 330: return true;
            default: return false;
        }
    }
}
