package dev.majek.pvptoggle;

import dev.majek.pvptoggle.hooks.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PvPToggle extends JavaPlugin {

    public static Map<UUID, Boolean> pvp = new HashMap<>();

    public static FileConfiguration config;
    public static boolean hasWorldGuard = false, hasRegionProtection = false, hasGriefPrevention = false,
            hasClaimChunk = false, hasLands = false, hasGriefDefender = false;
    public static PvPToggle instance;
    public PvPToggle() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getConsoleSender().sendMessage(format("&f[&4&lP&c&lv&4&lP&f] &aGetting player preferences..."));

        instance.saveDefaultConfig();
        File configFile = new File(instance.getDataFolder(), "config.yml"); String[] foo = new String[0];
        try {
            ConfigUpdater.update(instance, "config.yml", configFile, Arrays.asList(foo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.reloadConfig();
        config = this.getConfig();

        // Hook into soft dependencies
        goFishing();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static String format(String format) { // Format messages
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    public static PvPToggle getInstance() {
        return instance;
    }

    public boolean hasPvPOn(Player player) {
        return hasPvPOn(player.getUniqueId());
    }

    public boolean hasPvPOn(UUID uuid) {
        if (pvp.containsKey(uuid))
            return pvp.get(uuid);
        else {
            pvp.put(uuid, false);
            return false;
        }
    }

    public void setStatus(UUID uuid, boolean toggle) {
        if (pvp.containsKey(uuid))
            pvp.replace(uuid, toggle);
        else
            pvp.put(uuid, toggle);
    }

    /**
     * Hook into soft dependencies for placeholder and region support
     */
    public void goFishing() { // Get hooks lol
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
                this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooking into PlaceholderAPI...");
            new PlaceholderAPI(this).register();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("WorldGuard") &&
                this.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("Hooking into WorldGuard...");
            hasWorldGuard = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("RegionProtection") &&
                this.getServer().getPluginManager().getPlugin("RegionProtection") != null) {
            getLogger().info("Hooking into RegionProtection...");
            hasRegionProtection = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("GriefPrevention") &&
                this.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            getLogger().info("Hooking into GriefPrevention...");
            hasGriefPrevention = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("ClaimChunk") &&
                this.getServer().getPluginManager().getPlugin("ClaimChunk") != null) {
            getLogger().info("Hooking into ClaimChunk...");
            hasClaimChunk = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Lands") &&
                this.getServer().getPluginManager().getPlugin("Lands") != null) {
            getLogger().info("Hooking into Lands...");
            hasLands = true;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("GriefDefender") &&
                this.getServer().getPluginManager().getPlugin("GriefDefender") != null) {
            getLogger().info("Hooking into GriefDefender...");
            hasGriefDefender = true;
        }
    }
}
