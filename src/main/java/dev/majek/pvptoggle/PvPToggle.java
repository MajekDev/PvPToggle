package dev.majek.pvptoggle;

import dev.majek.pvptoggle.command.CommandAllPvP;
import dev.majek.pvptoggle.command.CommandBlockPvP;
import dev.majek.pvptoggle.command.CommandPvP;
import dev.majek.pvptoggle.data.*;
import dev.majek.pvptoggle.events.PlayerJoin;
import dev.majek.pvptoggle.events.PvPEvent;
import dev.majek.pvptoggle.hooks.PlaceholderAPI;
import dev.majek.pvptoggle.hooks.WorldGuard;
import dev.majek.pvptoggle.storage.JsonStorage;
import dev.majek.pvptoggle.storage.SqlStorage;
import dev.majek.pvptoggle.storage.StorageMethod;
import dev.majek.pvptoggle.util.Metrics;
import dev.majek.pvptoggle.api.PvPStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PvPToggle extends JavaPlugin {

  // This list gets wiped every reset - we don't really care about it
  public static List<UUID> inRegion = new CopyOnWriteArrayList<>();
  private final List<World> disabledWorlds;

  public static Boolean blockPvp = false;

  public static FileConfiguration config;
  public static boolean hasWorldGuard = false, hasRegionProtection = false,
      hasGriefPrevention = false, hasLands = false, hasGriefDefender = false;
  public static boolean debug = false;
  public static boolean consoleLog = false;

  private static MySQL sql;
  private static DataHandler dataHandler;
  private static PvPToggle instance;
  private static StorageMethod storageMethod;

  public PvPToggle() {
    instance = this;
    sql = new MySQL();
    dataHandler = new DataHandler();
    disabledWorlds = new ArrayList<>();
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
      ConfigUpdater.update(instance, "config.yml", configFile, Collections.emptyList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    instance.reloadConfig();
    config = this.getConfig();

    // Set config values
    debug = config.getBoolean("debug");
    consoleLog = config.getBoolean("console-log");

    // Set storage method
    if (config.getBoolean("database-enabled", false)) {
      storageMethod = new SqlStorage();
      try {
        sql.connect();
      } catch (SQLException e) {
        error("Failed to connect to MySQL database... defaulting to Json.");
        storageMethod = new JsonStorage();
      }
      if (sql.isConnected()) {
        Bukkit.getLogger().info("Successfully connected to MySQL database.");
        sql.createTable();
      }
    } else {
      storageMethod = new JsonStorage();
    }

    // Hook into soft dependencies
    getHooks();

    // Load disabled worlds
    for (String worldName : getConfig().getStringList("disabled-worlds")) {
      World world = Bukkit.getWorld(worldName);
      if (world != null)
        disabledWorlds.add(world);
    }

    // Metrics
    new Metrics(this, 7799);

    // Register commands and events
    registerCommands();
    this.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
    this.getServer().getPluginManager().registerEvents(new PvPEvent(), this);

    // Plugin successfully loaded
    Bukkit.getConsoleSender().sendMessage(format("&f[&4&lP&c&lv&4&lP&f] &aFinished loading PvPToggle v2 in "
        + (System.currentTimeMillis() - start) + "ms"));
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    if (sql.isConnected())
      sql.disconnect();
    // We don't really need to do anything here lol
    // Maybe save the hashmap just in case?
  }

  @SuppressWarnings("ConstantConditions")
  public void registerCommands() {
    this.getCommand("pvp").setExecutor(new CommandPvP());
    this.getCommand("pvp").setTabCompleter(new CommandPvP());
    this.getCommand("allpvp").setExecutor(new CommandAllPvP());
    this.getCommand("allpvp").setTabCompleter(new CommandAllPvP());
    this.getCommand("blockpvp").setExecutor(new CommandBlockPvP());
    this.getCommand("blockpvp").setTabCompleter(new CommandBlockPvP());
  }

  /**
   * Used internally for formatting bukkit color codes
   *
   * @param format the string to format
   * @return formatted string
   */
  public static String format(String format) {
    return ChatColor.translateAlternateColorCodes('&', format);
  }

  /**
   * Get main PvPToggle functions.
   * Note: if you're hooking into PvPToggle, this is what you want.
   *
   * @return PvPToggle instance.
   */
  public static PvPToggle core() {
    return instance;
  }

  public static MySQL sql() {
    return sql;
  }

  public static DataHandler dataHandler() {
    return dataHandler;
  }

  public static FileConfiguration config() {
    return core().getConfig();
  }

  public static StorageMethod storageMethod() {
    return storageMethod;
  }

  public List<World> disabledWorlds() {
    return disabledWorlds;
  }

  /**
   * Set a player's pvp status. This will call {@link PvPStatusChangeEvent}.
   *
   * @param player The player.
   * @param toggle True -> pvp on, false -> pvp off.
   */
  public void setStatus(Player player, boolean toggle) {
    PvPStatusChangeEvent statusChangeEvent = new PvPStatusChangeEvent(player, toggle);
    Bukkit.getPluginManager().callEvent(statusChangeEvent);
    if (statusChangeEvent.isCancelled()) {
      return;
    }
    setStatus(player.getUniqueId(), toggle);
  }

  /**
   * Set a player's pvp status. This will not call {@link PvPStatusChangeEvent}.
   *
   * @param uuid   The player's unique id.
   * @param toggle True -> pvp on, false -> pvp off.
   */
  public void setStatus(UUID uuid, boolean toggle) {
    User user = dataHandler().getUser(uuid);
    user.pvpStatus(toggle).updateUser();
    if (debug)
      Bukkit.getConsoleSender().sendMessage(format("&7[&cPvPToggle Debug&7] &f"
          + uuid + " -> " + toggle));
    if (consoleLog)
      Bukkit.getConsoleSender().sendMessage(format("&7[&cPvPToggle Log&7] &f" +
          Bukkit.getOfflinePlayer(uuid).getName() + "'s PvP status updated to: &b" + toggle));
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

  public static void log(@NotNull Object x) {
    core().getLogger().info(x.toString());
  }

  public static void error(@NotNull Object x) {
    core().getLogger().severe(x.toString());
  }

  public static void debug(@NotNull Object x) {
    if (debug) {
      core().getLogger().warning(x.toString());
    }
  }
}
