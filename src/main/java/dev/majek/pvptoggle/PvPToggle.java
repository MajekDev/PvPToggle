package dev.majek.pvptoggle;

import dev.majek.pvptoggle.command.CommandPvP;
import dev.majek.pvptoggle.events.PlayerJoin;
import dev.majek.pvptoggle.events.PvPEvent;
import dev.majek.pvptoggle.hooks.PlaceholderAPI;
import dev.majek.pvptoggle.hooks.WorldGuard;
import dev.majek.pvptoggle.mysql.MySQL;
import dev.majek.pvptoggle.mysql.SQLGetter;
import dev.majek.pvptoggle.sqlite.Database;
import dev.majek.pvptoggle.sqlite.SQLite;
import dev.majek.pvptoggle.util.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PvPToggle extends JavaPlugin {

    // For API reasons, this hashmap is not to be modified anywhere except the setStatus method
    protected Map<UUID, Boolean> pvp = new HashMap<>();
    // This list gets wiped every reset - we don't really care about it
    public static List<UUID> inRegion = new CopyOnWriteArrayList<>();

    public static FileConfiguration config;
    public static boolean hasWorldGuard = false, hasRegionProtection = false,
            hasGriefPrevention = false, hasLands = false, hasGriefDefender = false;
    public static boolean debug = false;
    public static boolean consoleLog = false;
    public static boolean usingMySQL = false;
    private Database db;
    public MySQL SQL;
    public SQLGetter data;
    public static PvPToggle instance;
    public PvPToggle() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        long start = System.currentTimeMillis();
        Bukkit.getConsoleSender().sendMessage(format("&f[&4&lP&c&lv&4&lP&f] &aGetting player preferences..."));

        // Load new config values if there are any
        instance.saveDefaultConfig();
        File configFile = new File(instance.getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(instance, "config.yml", configFile, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.reloadConfig();
        config = this.getConfig();

        // Set config values
        debug = config.getBoolean("debug");
        consoleLog = config.getBoolean("console-log");
        usingMySQL = config.getBoolean("database-enabled");

        // Hook into soft dependencies
        getHooks();

        // Load data from MySQL or SQLite
        this.SQL = new MySQL();
        this.data = new SQLGetter(this);
        if (usingMySQL) {
            try {
                SQL.connect();
            } catch (SQLException e) {
                //e.printStackTrace();
                this.getLogger().warning("Failed to connect to MySQL database... defaulting to SQLite.");
                loadSQLite(); // Try to load from SQLite if MySQL fails
            }
            if (SQL.isConnected()) {
                Bukkit.getLogger().info("Successfully connected to MySQL database.");
                data.createTable();
                data.getAllStatuses();
            }
        } else {
            loadSQLite();
        }

        // Register commands and events
        Objects.requireNonNull(this.getCommand("pvp")).setExecutor(new CommandPvP());
        this.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        this.getServer().getPluginManager().registerEvents(new PvPEvent(), this);

        // Plugin successfully loaded
        Bukkit.getConsoleSender().sendMessage(format("&f[&4&lP&c&lv&4&lP&f] &aFinished loading PvPToggle v2 in "
                + (System.currentTimeMillis() - start) + "ms"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (SQL.isConnected())
            SQL.disconnect();
        // We don't really need to do anything here lol
        // Maybe save the hashmap just in case?
    }

    /**
     * Used internally for formatting bukkit color codes
     * @param format the string to format
     * @return formatted string
     */
    public static String format(String format) {
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    /**
     * Get main PvPToggle functions
     * Note: if you're hooking into PvPToggle, this is what you want
     * @return PvPToggle instance
     */
    public static PvPToggle getCore() {
        return instance;
    }

    /**
     * Check if a player has never joined or has never been in the pvp hashmap
     * @param player player to check
     * @return true -> not found, false -> found
     */
    public boolean isNotInHashmap(Player player) {
        return !pvp.containsKey(player.getUniqueId());
    }

    /**
     * Get a player's pvp status
     * @param player requested online player
     * @return true -> pvp is on | false -> pvp is off
     */
    public boolean hasPvPOn(Player player) {
        return hasPvPOn(player.getUniqueId());
    }

    /**
     * Get a player's pvp status from uuid
     * @param uuid requested player's uuid
     * @return true -> pvp is on | false -> pvp is off
     */
    public boolean hasPvPOn(UUID uuid) {
        if (pvp.containsKey(uuid))
            return pvp.get(uuid);
        else {
            // Player is somehow not in the cache - add them
            pvp.put(uuid, config.getBoolean("default-pvp"));
            return config.getBoolean("default-pvp");
        }
    }

    /**
     * Set a player's pvp status
     * @param uuid the player's unique id
     * @param toggle true -> pvp on, false -> pvp off
     */
    public void setStatus(UUID uuid, boolean toggle) {
        if (pvp.containsKey(uuid))
            pvp.replace(uuid, toggle);
        else
            pvp.put(uuid, toggle);
        if (usingMySQL)
            this.data.updateStatus(uuid);
        else
            this.db.updatePlayer(uuid);
        if (debug)
            Bukkit.getConsoleSender().sendMessage(format("&7[&cPvPToggle Debug&7] &f"
                    + uuid.toString() + " -> " + toggle));
        if (consoleLog)
            Bukkit.getConsoleSender().sendMessage(format("&7[&cPvPToggle Log&7] &f" +
                    Bukkit.getOfflinePlayer(uuid).getName() + "'s PvP status updated to: &b" + toggle));
    }

    /**
     * Load pvp statuses saved in SQLite database
     */
    private void loadSQLite() {
        this.db = new SQLite(this);
        this.db.load();
        this.db.getPlayers();
        this.db.getPvPStatuses();
        this.getLogger().config("Loading pvp statuses from SQLite database...");
    }

    /**
     * Hook into soft dependencies for placeholder and region support
     */
    private void getHooks() {
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
                this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Hooking into PlaceholderAPI...");
            new PlaceholderAPI(this).register();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("WorldGuard") &&
                this.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("Hooking into WorldGuard...");
            hasWorldGuard = true;
            this.getServer().getPluginManager().registerEvents(new WorldGuard(), this);
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
