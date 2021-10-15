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

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.User;
import dev.majek.pvptoggle.message.Message;
import me.ryanhamshire.GriefPrevention.Claim;
import net.kyori.adventure.util.TriState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class GriefPreventionHook implements Listener, RegionHook {

  @Override
  public boolean isInRegion(@NotNull Player player) {
    return isInRegion(player.getLocation());
  }

  public boolean isInRegion(@NotNull Location location) {
    Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore
        .getClaimAt(location, false, null);
    return claim != null;
  }

  @Override
  public TriState getRegionToggle(@NotNull Player player) {
    return isClaimPvPSafeZone(player.getLocation()) ? TriState.FALSE : TriState.NOT_SET;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    // Check if the player actually moved or just moved their head
    if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY()
        == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
    Player player = event.getPlayer();
    User user = PvPToggle.userHandler().getUser(player);

    // Reset pvp status if the player is leaving a region
    if (user.inRegion()) {
      if (!isInRegion(event.getTo())) {
        PvPToggle.core().setStatus(player.getUniqueId(), PvPToggle.config().getBoolean("default-pvp", false));
        user.inRegion(false);
      }
    }

    check(event.getPlayer(), event.getTo());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    check(event.getPlayer(), event.getPlayer().getLocation());
  }

  public boolean isClaimPvPSafeZone(Location location) {
    Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore
        .getClaimAt(location, false, null);
    return me.ryanhamshire.GriefPrevention.GriefPrevention.instance.claimIsPvPSafeZone(claim);
  }

  public void check(Player player, Location location) {
    if (isClaimPvPSafeZone(location)) {
      Message.REGION_ENTER.send(player, false);
      PvPToggle.core().setStatus(player.getUniqueId(), false);
      PvPToggle.userHandler().getUser(player).inRegion(true);
    }
  }
}