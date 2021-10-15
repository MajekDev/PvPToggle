/*
 * This file is part of PvPToggle, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Majekdor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.majek.pvptoggle;

import com.tchristofferson.configupdater.ConfigUpdater;
import dev.majek.pvptoggle.api.PvPStatusChangeEvent;
import dev.majek.pvptoggle.api.PvPToggleApi;
import dev.majek.pvptoggle.command.CommandAllPvP;
import dev.majek.pvptoggle.command.CommandBlockPvP;
import dev.majek.pvptoggle.command.CommandPvP;
import dev.majek.pvptoggle.command.CommandPvPReload;
import dev.majek.pvptoggle.data.User;
import dev.majek.pvptoggle.data.UserHandler;
import dev.majek.pvptoggle.data.MySQL;
import dev.majek.pvptoggle.event.PlayerJoin;
import dev.majek.pvptoggle.event.PvPEvent;
import dev.majek.pvptoggle.hook.HookManager;
import dev.majek.pvptoggle.storage.JsonStorage;
import dev.majek.pvptoggle.storage.SqlStorage;
import dev.majek.pvptoggle.storage.StorageMethod;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Main plugin class.</p>
 * <p>Use {@link #core()} to access core plugin utilities such as nickname storage.</p>
 * <p>Use {@link #api()} to access api utilities such as event management.</p>
 */
public final class PvPToggle extends JavaPlugin {

  private static PvPToggle          instance;
  private static PvPToggleApi api;
  private static FileConfiguration  config;
  private static MySQL              sql;
  private static StorageMethod      storageMethod;
  private static UserHandler        userHandler;
  private static HookManager        hookManager;

  private final Set<World> disabledWorlds;
  private final Metrics metrics;

  /**
   * Initialize plugin.
   */
  public PvPToggle() {
    instance = this;
    api = new PvPToggleApi();
    sql = new MySQL();
    userHandler = new UserHandler();
    hookManager = new HookManager();
    disabledWorlds = new HashSet<>();
    metrics = new Metrics(this, 7799);
  }

  /**
   * Plugin startup logic.
   */
  @Override
  public void onEnable() {
    reload();
    // Set storage method
    if (config.getBoolean("database-enabled", false)) {
      try {
        sql.connect();
      } catch (SQLException e) {
        error("Failed to connect to MySQL database... defaulting to Json.");
        storageMethod = new JsonStorage();
      }
      if (sql.isConnected()) {
        Bukkit.getLogger().info("Successfully connected to MySQL database.");
        storageMethod = new SqlStorage();
        sql.createTable();
      }
    } else {
      storageMethod = new JsonStorage();
    }
    storageMethod.loadAllUsers();

    // Register commands
    registerCommands();

    // Register listeners
    registerEvents(new PvPEvent(), new PlayerJoin());

    // Custom chart to see how many people enable default pvp
    metrics.addCustomChart(new SimplePie("default_pvp",
        () -> String.valueOf(config().getBoolean("default_pvp", false))));
  }

  @SuppressWarnings("ConstantConditions")
  private void registerCommands() {
    this.getCommand("pvp").setExecutor(new CommandPvP());
    this.getCommand("pvp").setTabCompleter(new CommandPvP());
    this.getCommand("allpvp").setExecutor(new CommandAllPvP());
    this.getCommand("allpvp").setTabCompleter(new CommandAllPvP());
    this.getCommand("blockpvp").setExecutor(new CommandBlockPvP());
    this.getCommand("blockpvp").setTabCompleter(new CommandBlockPvP());
    this.getCommand("pvptogglereload").setExecutor(new CommandPvPReload());
    this.getCommand("pvptogglereload").setTabCompleter(new CommandPvPReload());
  }

  private void registerEvents(Listener... listeners) {
    for (Listener listener : listeners) {
      getServer().getPluginManager().registerEvents(listener, this);
    }
  }

  /**
   * Plugin shutdown logic.
   */
  @Override
  public void onDisable() {
    if (sql.isConnected())
      sql.disconnect();
  }

  public static PvPToggle core() {
    return instance;
  }

  public static PvPToggleApi api() {
    return api;
  }

  public static FileConfiguration config() {
    return config;
  }

  public static MySQL sql() {
    return sql;
  }

  public static StorageMethod storageMethod() {
    return storageMethod;
  }

  public static UserHandler userHandler() {
    return userHandler;
  }

  public static HookManager hookManager() {
    return hookManager;
  }

  public void reload() {
    // Reload config for changed values
    instance.saveDefaultConfig();
    File configFile = new File(instance.getDataFolder(), "config.yml");
    try {
      ConfigUpdater.update(instance, "config.yml", configFile, Collections.emptyList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    instance.reloadConfig();
    config = instance.getConfig();

    // Load disabled worlds
    for (String worldName : getConfig().getStringList("disabled-worlds")) {
      World world = Bukkit.getWorld(worldName);
      if (world != null)
        disabledWorlds.add(world);
    }

    // Reload hooks
    hookManager.reload();
  }

  public void sendMessage(@NotNull CommandSender sender, @NotNull Component message) {
    BukkitAudiences.create(core()).sender(sender).sendMessage(message);
  }

  /**
   * Set a player's pvp status. This will call {@link PvPStatusChangeEvent}.
   *
   * @param player The player.
   * @param toggle Whether pvp is on.
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
   * @param toggle Whether pvp is on.
   */
  public void setStatus(UUID uuid, boolean toggle) {
    User user = userHandler().getUser(uuid);
    user.pvpStatus(toggle).updateUser();
    debug(uuid + " -> " + toggle);
    if (config().getBoolean("console-log", false)) {
      log(Bukkit.getOfflinePlayer(uuid).getName() + "'s PvP status updated to: &b" + toggle);
    }
  }

  public Set<World> disabledWorlds() {
    return disabledWorlds;
  }

  public static void log(@NotNull Object x) {
    core().getLogger().info(x.toString());
  }

  public static void error(@NotNull Object x) {
    core().getLogger().severe(x.toString());
  }

  public static void debug(@NotNull Object x) {
    if (config.getBoolean("debug", false)) {
      core().getLogger().warning(x.toString());
    }
  }
}