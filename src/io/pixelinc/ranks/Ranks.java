package io.pixelinc.ranks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import io.pixelinc.ranks.commands.GroupCommand;
import io.pixelinc.ranks.commands.RankCommand;
import io.pixelinc.ranks.utils.PEXHelper;

public class Ranks extends JavaPlugin {

    public PEXHelper pexHelper;

    @Override
    public void onEnable() {
        // Check if the server has PermissionsEX loaded.
        if (Bukkit.getPluginManager().getPlugin("PermissionsEX") == null) {
            getLogger().warning("Disabling due to PermissionsEX not being loaded!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.pexHelper = new PEXHelper();

        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("group").setExecutor(new GroupCommand(this));
    }

    @Override
    public void onDisable() {
    }

}
