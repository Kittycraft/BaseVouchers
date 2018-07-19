package org.thelockj.basevouchersupport;

import org.bukkit.plugin.java.JavaPlugin;

public class BaseVoucher extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("basevoucher").setExecutor(new BaseCommand());
        getLogger().info("Enabled Base Voucher Support!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Enabled Base Voucher Support!");
    }
}
