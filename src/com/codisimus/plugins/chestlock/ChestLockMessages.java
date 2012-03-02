package com.codisimus.plugins.chestlock;

import org.bukkit.Material;

/**
 * Holds messages that are displayed to users of this plugin
 *
 * @author Codisimus
 */
public class ChestLockMessages {
    private static String permission;
    private static String lock;
    private static String locked;
    private static String unlock;
    private static String keySet;
    private static String invalidKey;
    private static String unlockable;
    private static String lockable;
    private static String doNotOwn;
    private static String own;
    private static String disown;
    private static String limit;
    private static String clear;
    private static String insufficientFunds;
    
    public static void setPermissionMsg(String msg) {
        permission = format(msg);
    }
    
    public static void setLockMsg(String msg) {
        lock = format(msg);
    }
    
    public static void setLockedMsg(String msg) {
        locked = format(msg);
    }
    
    public static void setUnlockMsg(String msg) {
        unlock = format(msg);
    }
    
    public static void setKeySetMsg(String msg) {
        keySet = format(msg);
    }
    
    public static void setInvalidKeyMsg(String msg) {
        invalidKey = format(msg);
    }
    
    public static void setUnlockableMsg(String msg) {
        unlockable = format(msg);
    }
    
    public static void setLockableMsg(String msg) {
        lockable = format(msg);
    }
    
    public static void setDoNotOwnMsg(String msg) {
        doNotOwn = format(msg);
    }
    
    public static void setOwnMsg(String msg) {
        own = format(msg);
    }
    
    public static void setDisownMsg(String msg) {
        disown = format(msg);
    }
    
    public static void setLimitMsg(String msg) {
        limit = format(msg);
    }
    
    public static void setClearMsg(String msg) {
        clear = format(msg);
    }
    
    public static void setInsufficientFundsMsg(String msg) {
        insufficientFunds = format(msg);
    }
    
    public static String getPermissionMsg() {
        return permission;
    }
    
    public static String getLockMsg(Material blockType) {
        return lock.replace("<blocktype>", toString(blockType));
    }
    
    public static String getLockedMsg(Material blockType) {
        return locked.replace("<blocktype>", toString(blockType));
    }
    
    public static String getUnlockMsg(Material blockType) {
        return unlock.replace("<blocktype>", toString(blockType));
    }
    
    public static String getKeySetMsg(Material itemType) {
        return keySet.replace("<itemtype>", toString(itemType));
    }
    
    public static String getInvalidKeyMsg(Material itemType) {
        return invalidKey.replace("<itemtype>", toString(itemType));
    }
    
    public static String getUnlockableMsg(Material blockType) {
        return unlockable.replace("<blocktype>", toString(blockType));
    }
    
    public static String getLockableMsg(Material blockType) {
        return lockable.replace("<blocktype>", toString(blockType));
    }
    
    public static String getDoNotOwnMsg(Material blockType) {
        return doNotOwn.replace("<blocktype>", toString(blockType));
    }
    
    public static String getOwnMsg(Material blockType) {
        return own.replace("<blocktype>", toString(blockType));
    }
    
    public static String getDisownMsg(Material blockType) {
        return disown.replace("<blocktype>", toString(blockType));
    }
    
    public static String getLimitMsg(Material blockType) {
        return limit.replace("<blocktype>", toString(blockType));
    }
    
    public static String getClearMsg() {
        return clear;
    }
    
    public static String getInsufficientFundsMsg(double amount, Material blockType) {
        return insufficientFunds.replace("<blocktype>", toString(blockType)).replace("<price>", Econ.format(amount));
    }
    
    private static String toString(Material type) {
        return type.name().toLowerCase();
    }
    
    /**
     * Adds various Unicode characters and colors to a string
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