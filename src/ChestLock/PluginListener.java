
package ChestLock;

import org.bukkit.event.server.ServerListener;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Checks for plugins whenever one is enabled
 *
 */
public class PluginListener extends ServerListener {
    public PluginListener() { }
    private Methods methods = new Methods();
    protected static Boolean useOP;

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (ChestLock.permissions == null && !useOP) {
            Plugin permissions = ChestLock.pm.getPlugin("Permissions");
            if (permissions != null) {
                    ChestLock.permissions = ((Permissions)permissions).getHandler();
                    System.out.println("[ChestLock] Successfully linked with Permissions!");
            }
        }
        if (Register.economy == null)
            System.err.println("[ChestLock] Config file outdated, Please regenerate");
        else if (!Register.economy.equalsIgnoreCase("none") && !methods.hasMethod()) {
            try {
                methods.setMethod(ChestLock.pm.getPlugin(Register.economy));
                if (methods.hasMethod()) {
                    Register.econ = methods.getMethod();
                    System.out.println("[ChestLock] Successfully linked with "+
                            Register.econ.getName()+" "+Register.econ.getVersion()+"!");
                }
            }
            catch (Exception e) {
            }
        }
    }
}
