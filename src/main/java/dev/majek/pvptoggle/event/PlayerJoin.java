/*
 * This file is part of PvPToggle, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Majekdor
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    User user = PvPToggle.storageMethod().getUser(player.getUniqueId());
    if (user == null) {
      user = PvPToggle.userHandler().getUser(player);
    }
    // Turn pvp off based on config option
    if (PvPToggle.config().getBoolean("off-on-join", false)) {
      user.pvpStatus(false);
    }
    PvPToggle.storageMethod().updateUser(user);
  }
}
