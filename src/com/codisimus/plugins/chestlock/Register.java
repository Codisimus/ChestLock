package com.codisimus.plugins.chestlock;

import com.codisimus.plugins.chestlock.register.payment.Method;
import com.codisimus.plugins.chestlock.register.payment.Method.MethodAccount;
import org.bukkit.entity.Player;

/**
 * Manages payment for buying and selling Chunks
 * Using Nijikokun's Register API
 *
 * @author Codisimus
 */
public class Register {
    public static String economy;
    public static Method econ;
    public static String insufficientFunds;

    /**
     * Subtracts a specific amount from the players total balance
     * Returns true if the transaction was successful
     * 
     * @param player The Player who was is being charged money
     * @param amount The amount which will be charged
     * @param type The type of block
     * @return true if the transaction was successful
     */
    public static boolean charge(Player player, double amount, String type) {
        MethodAccount account = econ.getAccount(player.getName());
        if (!account.hasEnough(amount)) {
            player.sendMessage(insufficientFunds.replaceAll("<price>", format(amount)).replaceAll("<blocktype>", type));
            return false;
        }
        account.subtract(amount);
        return true;
    }
    
    /**
     * Formats the money amount by adding the unit
     * 
     * @param amount The amount of money to be formatted
     * @return The String of the amount + currency name
     */
    public static String format(double amount) {
        return econ.format(amount);
    }
}
