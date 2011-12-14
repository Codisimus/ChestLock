package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.SaveSystem;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Loads ChestLock data for each World that is loaded
 *
 * @author Codisimus
 */
public class WorldLoadListener extends WorldListener{

    /**
     * Loads ChestLock data for the loaded World
     * 
     * @param event The WorldLoadEvent that occurred
     */
    @Override
    public void onWorldLoad (WorldLoadEvent event) {
        SaveSystem.load(event.getWorld());
    }
}

