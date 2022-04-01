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
package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.message.Message;
import dev.majek.pvptoggle.util.TabCompleterBase;
import dev.majek.pvptoggle.util.TimeInterval;
import net.kyori.adventure.util.TriState;
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
    // Changing another player's status
    if (args.length > 1) {
      // Make sure the sender has permission
      if (!sender.hasPermission("pvptoggle.others"))   {
        Message.NO_PERMISSION.send(sender);
        return true;
      }

      // Get target player
      Player target = Bukkit.getPlayer(args[1]);

      // Make sure target is online
      if (target == null) {
        Message.UNKNOWN_PLAYER.send(sender, args[1]);
        return true;
      }

      TriState triState = this.toggle(args[0], target);
      if (triState == TriState.NOT_SET) {
        return false;
      }
      boolean toggle = triState == TriState.TRUE;

      // Check if a target is in a region that doesn't allow pvp toggling
      if (PvPToggle.hookManager().isInRegion(target) && PvPToggle.hookManager().getRegionToggle(target) != TriState.NOT_SET) {
        Message.REGION_DENY.send(sender, PvPToggle.hookManager().getRegionToggle(target) == TriState.TRUE, false, target);
        return true;
      }

      // Check if the target is in a world where the command is disabled
      if (PvPToggle.core().disabledWorlds().contains(target.getWorld())) {
        Message.DISABLED_WORLD.send(sender, false, target);
        return true;
      }

      // Change target status and send message
      Message.PVP_TOGGLE_OTHER.send(sender, toggle, target);
      PvPToggle.core().setStatus(target, toggle);
      Message.PVP_CHANGED.send(target, toggle);
    }

    // Player is specifying on or off for themselves
    else if (args.length == 1) {
      if (!(sender instanceof Player)) {
        return false;
      }
      Player player = (Player) sender;

      // Check if the player is in a world where the command is disabled
      if (PvPToggle.core().disabledWorlds().contains(player.getWorld())) {
        Message.DISABLED_WORLD.send(player, true, null);
        return true;
      }

      // Check if the command is on cooldown
      if (PvPToggle.config().getInt("pvp-cooldown", 60) > 0 && cooldownMap.containsKey(player)
          && !player.hasPermission("pvptoggle.cooldown.bypass")) {
        long timeSinceLast = (System.currentTimeMillis() - cooldownMap.get(player));
        if ((timeSinceLast / 1000L) < PvPToggle.config().getInt("pvp-cooldown", 60)) {
          long useIn = (PvPToggle.config().getInt("pvp-cooldown", 60) * 1000L) - timeSinceLast;
          Message.ON_COOLDOWN.send(player, TimeInterval.formatTime(useIn, true));
          return true;
        }
      }

      // Check if the player is blocked from changing their status
      if (PvPToggle.userHandler().getUser(player).pvpBlocked()) {
        Message.PVP_BLOCKED.send(player);
        return true;
      }

      // Check if a player is in a region that doesn't allow pvp toggling
      if (PvPToggle.hookManager().isInRegion(player) && PvPToggle.hookManager().getRegionToggle(player) != TriState.NOT_SET) {
        Message.REGION_DENY.send(sender, PvPToggle.hookManager().getRegionToggle(player) == TriState.TRUE, true, null);
        return true;
      }

      TriState triState = this.toggle(args[0], player);
      if (triState == TriState.NOT_SET) {
        return false;
      }
      boolean toggle = triState == TriState.TRUE;

      // Set pvp status
      PvPToggle.core().setStatus(player.getUniqueId(), toggle);
      cooldownMap.put(player, System.currentTimeMillis());
      if (toggle) {
        Message.PVP_ENABLED.send(player);
      } else {
        Message.PVP_DISABLED.send(player);
      }
    } else {
      if (!(sender instanceof Player)) {
        return false;
      }

      Player player = (Player) sender;
      Message.YOUR_PVP.send(player, PvPToggle.userHandler().getUser(player).pvpStatus());
    }
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1)
      return TabCompleterBase.filterStartingWith(args[0], Arrays.asList("on", "off", "toggle"));
    else if (args.length == 2)
      return TabCompleterBase.filterStartingWith(args[1], sender.hasPermission("pvptoggle.others") ?
          TabCompleterBase.getOnlinePlayers(args[1]) : Collections.emptyList());
    else
      return Collections.emptyList();
  }

  private @NotNull TriState toggle(final @NotNull String toggle, final @NotNull Player target) {
    switch (toggle) {
      case "on":
      case "true":
        return TriState.TRUE;
      case "off":
      case "false":
        return TriState.FALSE;
      case "toggle":
      case "swap":
        return TriState.byBoolean(!PvPToggle.userHandler().getUser(target).pvpStatus());
      default:
        return TriState.NOT_SET;
    }
  }
}