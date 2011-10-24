package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.SaveSystem;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Loads Turnstile data for each World that is loaded
 *
 * @author Codisimus
 */
public class worldListener extends WorldListener{

    @Override
    public void onWorldLoad (WorldLoadEvent event) {
        SaveSystem.load(event.getWorld());
    }
}

