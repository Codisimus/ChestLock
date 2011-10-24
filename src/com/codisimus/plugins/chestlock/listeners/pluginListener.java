package com.codisimus.plugins.chestlock.listeners;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.Register;
import org.bukkit.event.server.ServerListener;
import com.codisimus.plugins.chestlock.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Checks for Permission/Economy plugins whenever a Plugin is enabled
 * 
 * @author Codisimus
 */
public class pluginListener extends ServerListener {
    public pluginListener() { }
    public static boolean useBP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        linkPermissions();
        linkEconomy();
    }

    /**
     * Find and link a Permission plugin
     *
     */
    public void linkPermissions() {
        //Return if we have already have a permissions plugin
        if (ChestLock.permissions != null)
            return;

        //Return if PermissionsEx is not enabled
        if (!ChestLock.pm.isPluginEnabled("PermissionsEx"))
            return;

        //Return if OP permissions will be used
        if (useBP)
            return;

        ChestLock.permissions = PermissionsEx.getPermissionManager();
        System.out.println("[ChestLock] Successfully linked with PermissionsEx!");
    }

    /**
     * Find and link an Economy plugin
     *
     */
    public void linkEconomy() {
        //Return if we already have an Economy plugin
        if (Methods.hasMethod())
            return;

        //Return if no Economy is wanted
        if (Register.economy.equalsIgnoreCase("none"))
            return;

        //Set preferred plugin if there is one
        if (!Register.economy.equalsIgnoreCase("auto"))
            Methods.setPreferred(Register.economy);

        Methods.setMethod(ChestLock.pm);

        //Reset Methods if the preferred Economy was not found
        if (!Methods.getMethod().getName().equalsIgnoreCase(Register.economy) && !Register.economy.equalsIgnoreCase("auto")) {
            Methods.reset();
            return;
        }

        Register.econ = Methods.getMethod();
        System.out.println("[ChestLock] Successfully linked with "+Register.econ.getName()+" "+Register.econ.getVersion()+"!");
    }
}
