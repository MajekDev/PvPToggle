package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.hooks.GriefPrevention;
import dev.majek.pvptoggle.hooks.WorldGuard;
import dev.majek.pvptoggle.util.TabCompleterBase;
import dev.majek.pvptoggle.util.TimeInterval;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandPvP implements TabExecutor {

  public static Map<Player, Long> cooldownMap = new HashMap<>();

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

    // Check if we're changing another player's status
    if (args.length > 1) {

      // Make sure the sender has permission
      if (!sender.hasPermission("pvptoggle.others"))   {
        sender.sendMessage(PvPToggle.config().getString("no-permission", "&cYou do not have permission to do that!"));
        return true;
      }

      // Get toggle
      boolean toggle;
      if (args[0].equalsIgnoreCase("on")) {
        toggle = true;
      } else if (args[0].equalsIgnoreCase("off")) {
        toggle = false;
      } else {
        return false; // Send usage
      }

      // Get target player
      Player target = Bukkit.getPlayer(args[1]);

      // Make sure target is online
      if (target == null) {
        sender.sendMessage(PvPToggle.format(PvPToggle.config().getString("unknown-player",
            "&cUnable to locate player %player%.").replace("%player%", args[1])));
        return true;
      }

      // Check if a target is in a region that doesn't allow pvp toggling
      if (playerIsInRegion(target) && getRegionToggle(target) != null) {
        sender.sendMessage(PvPToggle.format(PvPToggle.config().getString("region-deny",
            "&c%noun% in a region with PvP forced %toggle%.").replace("%noun%", PvPToggle.config()
            .getString("player is", "%player% is").replace("%target%", target.getName()))
            .replace("%toggle%", getRegionToggle(target) ? PvPToggle.config().getString("forced-on", "on")
                : PvPToggle.config().getString("forced-off", "off"))));
        return true;
      }

      // Change target status and send message
      sender.sendMessage(PvPToggle.format(PvPToggle.config().getString("pvp-toggle-other",
          "&6PvP is now %toggle% for &b%player%&6.").replace("%toggle%", toggle ? PvPToggle.config()
          .getString("forced-on", "on") : PvPToggle.config().getString("forced-off", "off"))
          .replace("%player%", target.getName())));
      PvPToggle.core().setStatus(target, toggle);
      target.sendMessage(PvPToggle.format(PvPToggle.config().getString(toggle ? "pvp-enabled" : "pvp-disabled")));
    }

    else if (args.length == 1) {

      if (!(sender instanceof Player)) {
        return false;
      }

      Player player = (Player) sender;

      // Check if the player is in a world where the command is disabled
      if (PvPToggle.core().disabledWorlds().contains(player.getWorld())) {
        player.sendMessage(PvPToggle.format(PvPToggle.config().getString("disabled-world",
            "&cYou are in a world where this command is disabled.")));
        return true;
      }

      // Check if the command is on cooldown
      if (PvPToggle.config().getInt("pvp-cooldown", 60) > 0 && cooldownMap.containsKey(player)) {
        long timeSinceLast = (System.currentTimeMillis() - cooldownMap.get(player));
        if ((timeSinceLast / 1000L) < PvPToggle.config().getInt("pvp-cooldown", 60)) {
          long useIn = (PvPToggle.config().getInt("pvp-cooldown", 60) * 1000L) - timeSinceLast;
          player.sendMessage(PvPToggle.format(PvPToggle.config().getString("on-cooldown",
              "&cYou may use this command again in %cooldown%.")
              .replace("%cooldown%", TimeInterval.formatTime(useIn, true))));
          return true;
        }
      }

      // Check if the player is blocked from changing their status
      if (PvPToggle.dataHandler().getUser(player).pvpBlocked()) {
        player.sendMessage(PvPToggle.format(PvPToggle.config().getString("pvp-blocked", "&6Toggling PvP is blocked.")));
        return true;
      }

      // Check if a player is in a region that doesn't allow pvp toggling
      if (playerIsInRegion(player) && getRegionToggle(player) != null) {
        player.sendMessage(PvPToggle.format(PvPToggle.config().getString("region-deny",
            "&c%noun% in a region with PvP forced %toggle%.").replace("%noun%", PvPToggle.config()
            .getString("you-are", "You are")).replace("%toggle%", getRegionToggle(player) ? PvPToggle
            .config().getString("forced-on", "on") : PvPToggle.config().getString("forced-off", "off"))));
        return true;
      }

      // Set pvp status
      if (args[0].equalsIgnoreCase("on")) {
        PvPToggle.core().setStatus(player.getUniqueId(), true);
        player.sendMessage(PvPToggle.format(PvPToggle.config().getString("pvp-enabled")));
        cooldownMap.put(player, System.currentTimeMillis());
      } else if (args[0].equalsIgnoreCase("off")) {
        PvPToggle.core().setStatus(player.getUniqueId(), false);
        player.sendMessage(PvPToggle.format(PvPToggle.config().getString("pvp-disabled")));
        cooldownMap.put(player, System.currentTimeMillis());
      } else {
        return false;
      }
    }
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1)
      return TabCompleterBase.filterStartingWith(args[0], Arrays.asList("on", "off"));
    else if (args.length == 2)
      return TabCompleterBase.filterStartingWith(args[1], sender.hasPermission("pvptoggle.others") ?
          TabCompleterBase.getOnlinePlayers(args[1]) : Collections.emptyList());
    else
      return Collections.emptyList();
  }

  /**
   * Check if a player is in a region
   *
   * @param player the player to check
   * @return true if in region, false if not
   */
  public boolean playerIsInRegion(@NotNull Player player) {
    if (PvPToggle.hasGriefPrevention && PvPToggle.hasWorldGuard) {
      return GriefPrevention.playerInRegion(player.getLocation()) || WorldGuard.isInRegion(player);
    }
    if (PvPToggle.hasWorldGuard) {
      return WorldGuard.isInRegion(player);
    }
    if (PvPToggle.hasGriefPrevention) {
      return GriefPrevention.playerInRegion(player.getLocation());
    }
    return false;
  }

  /**
   * Check if the region a player is in specifies a pvp toggle
   *
   * @param player the player who's location to we want to check
   * @return true: pvp forced on | false: pvp forced off | null: not specified
   */
  public Boolean getRegionToggle(@NotNull Player player) {
    try {
      if (PvPToggle.hasGriefPrevention && PvPToggle.hasWorldGuard) {
        return GriefPrevention.getRegionToggle(player.getLocation()) || WorldGuard.getRegionToggle(player);
      }
      if (PvPToggle.hasWorldGuard) {
        return WorldGuard.getRegionToggle(player);
      }
      if (PvPToggle.hasGriefPrevention) {
        return GriefPrevention.getRegionToggle(player.getLocation());
      }
      return null;
    } catch (NullPointerException ex) {
      return null;
    }
  }
}
