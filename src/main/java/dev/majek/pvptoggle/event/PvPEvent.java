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
package dev.majek.pvptoggle.event;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.User;
import dev.majek.pvptoggle.hook.LandsHook;
import dev.majek.pvptoggle.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PvPEvent implements Listener {

  // We CANNOT let anything cancel this - this is everything that prevents players from being damaged
  @EventHandler(ignoreCancelled = true)
  public void onPlayerPvP(EntityDamageByEntityEvent event) {
    // Ignore if it's not a player being damaged
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    // If the player doesn't have permission we're not helping them :)
    if (PvPToggle.config().getBoolean("use-permissions") && !event.getEntity().hasPermission("pvptoggle.use")) {
      return;
    }

    // Handle damage from Citizens NPCs
    if (event.getDamager().hasMetadata("NPC") || event.getEntity().hasMetadata("NPC")) {
      return;
    }

    // Handle damage from projectiles
    Player damager = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
    if (damager == null && event.getDamager() instanceof Projectile) {
      Projectile projectile = (Projectile) event.getDamager();
      damager = projectile.getShooter() instanceof Player ? (Player) projectile.getShooter() : null;
    }

    // Ignore if the player is damaging themselves or an entity shot a projectile at them
    if (damager == null || damager == event.getEntity() || damager.hasMetadata("NPC")) {
      return;
    }

    Player attacked = (Player) event.getEntity();
    User attackedUser = PvPToggle.userHandler().getUser(attacked);
    User damagerUser = PvPToggle.userHandler().getUser(damager);

    // Check if the player is in a world where the command is disabled
    if (PvPToggle.core().disabledWorlds().contains(damager.getWorld())) {
      return;
    }

    // Cancel event and send messages
    if (PvPToggle.hookManager().isLandsHooked()) {
      if (!LandsHook.canPvP(damager, attacked)) {
        Message.REGION_DENY.send(damager, false, true, null);
        event.setCancelled(true);
        event.getEntity().setFireTicks(-1);
        return;
      }
    }

    if (!damagerUser.pvpStatus()) {
      Message.NO_PVP.send(damager);
      event.setCancelled(true);
      event.getEntity().setFireTicks(-1);
    } else if (!attackedUser.pvpStatus()) {
      Message.OTHER_PVP.send(damager, attacked);
      event.setCancelled(true);
      event.getEntity().setFireTicks(-1);
    }
  }
}
