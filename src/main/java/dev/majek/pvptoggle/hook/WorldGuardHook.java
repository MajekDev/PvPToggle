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
package dev.majek.pvptoggle.hook;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.User;
import dev.majek.pvptoggle.message.Message;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * WorldGuard region check and listener if the plugin is active
 */
public class WorldGuardHook implements Listener, RegionHook {

  public static WorldGuardPlugin worldGuardPlugin;

  public static @NotNull WorldGuardPlugin getWorldGuard() {
    Plugin wgplugin = PvPToggle.core().getServer().getPluginManager().getPlugin("WorldGuard");
    if (!(wgplugin instanceof WorldGuardPlugin)) {
      throw new RuntimeException("Plugin named WorldGuard is not actually WorldGuard.");
    }
    return (WorldGuardPlugin) wgplugin;
  }

  @Override
  public boolean isInRegion(@NotNull Player player) {
    worldGuardPlugin = getWorldGuard();
    LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
    Location loc = localPlayer.getLocation();
    RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();
    ApplicableRegionSet set = query.getApplicableRegions(loc);
    return set.size() > 0; // Set size is 0 if player is in __global__ region
  }

  @Override
  public TriState getRegionToggle(@NotNull Player player) {
    LocalPlayer localPlayer = getWorldGuard().wrapPlayer(player);
    ApplicableRegionSet set = set(player);

    if (set.testState(localPlayer, Flags.PVP) && PvPToggle.core().getConfig()
        .getBoolean("force-pvp-in-region-allow"))
      return TriState.TRUE;
    else if (!(set.testState(localPlayer, Flags.PVP)))
      return TriState.FALSE;
    else
      return TriState.NOT_SET;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    // Check if the player actually moved or just moved their head
    if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY()
        == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
    checkRegion(event.getPlayer(), true);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    checkRegion(event.getPlayer(), false);
  }

  private void checkRegion(Player player, boolean fromPlayerMove) {
    // Get region and region information
    LocalPlayer localPlayer = getWorldGuard().wrapPlayer(player);
    ApplicableRegionSet set = set(player);

    // Check if the player enters the global region from a region
    User user = PvPToggle.userHandler().getUser(player);
    if (set.size() == 0 && fromPlayerMove) {
      if (user.inRegion()) {
        PvPToggle.core().setStatus(player.getUniqueId(), PvPToggle.config().getBoolean("default-pvp"));
        user.inRegion(false);
      }

      // Check if the player enters into a region
    } else if (set.size() > 0 && !user.inRegion()) {
      if (set.testState(localPlayer, Flags.PVP) && PvPToggle.core().getConfig()
          .getBoolean("force-pvp-in-region-allow")) {
        if (PvPToggle.config().getBoolean("region-notify", true))
          Message.REGION_ENTER.send(player, true);
        PvPToggle.core().setStatus(player.getUniqueId(), true);
      } else if (!(set.testState(localPlayer, Flags.PVP))) {
        if (PvPToggle.config().getBoolean("region-notify", true))
          Message.REGION_ENTER.send(player, false);
        PvPToggle.core().setStatus(player.getUniqueId(), false);
      }
      user.inRegion(true);
      // If the flag is neither on or off we don't need to do anything
    }
  }

  private static ApplicableRegionSet set(Player player) {
    LocalPlayer localPlayer = getWorldGuard().wrapPlayer(player);
    Location loc = localPlayer.getLocation();
    RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
    RegionQuery query = container.createQuery();
    return query.getApplicableRegions(loc);
  }
}
