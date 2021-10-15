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
package dev.majek.pvptoggle.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PvPStatusChangeEvent extends Event implements Cancellable {

  private final Player player;
  private final boolean status;
  private boolean isCanceled;

  public PvPStatusChangeEvent(Player player, boolean status) {
    this.player = player;
    this.status = status;
    this.isCanceled = false;
  }

  @Override
  public boolean isCancelled() {
    return this.isCanceled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.isCanceled = cancel;
  }

  private static final HandlerList HANDLERS = new HandlerList();

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  /**
   * Returns the player that's status is being changed.
   *
   * @return Player that's status is being changed.
   */
  public Player getPlayer() {
    return this.player;
  }

  /**
   * Returns the value that the player's PvP status is being changed to.
   *
   * @return new PvP status value.
   */
  public boolean getNewStatus() {
    return this.status;
  }

  /**
   * Returns the value that the player's PvP status was previously.
   *
   * @return previous PvP status value.
   */
  public boolean getOldStatus() {
    return !this.status;
  }
}
