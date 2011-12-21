package com.codisimus.plugins.chestlock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

/**
 * Manages payment of owning/locking Chests
 * 
 * @author Codisimus
 */
public class Econ {
    public static Economy economy;
    static String insufficientFunds;

    /**
     * Charges a Player a given amount of money, which goes to a Player/Bank
     * 
     * @param player The name of the Player to be charged
     * @param source The Player/Bank that will receive the money
     * @param amount The amount that will be charged
     * @return True if the transaction was successful
     */
    public static boolean charge(Player player, double amount, String type) {
        String name = player.getName();
        
        //Cancel if the Player cannot afford the transaction
        if (!economy.has(name, amount)) {
            player.sendMessage(insufficientFunds.replaceAll("<price>", economy.format(amount)).replaceAll("<blocktype>", type));
            return false;
        }
        
        economy.withdrawPlayer(name, amount);
        return true;
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    public static String format(double amount) {
        return economy.format(amount).replace(".00", "");
    }
}
