package com.arcunis.resourcepack;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new Resourcepack(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
