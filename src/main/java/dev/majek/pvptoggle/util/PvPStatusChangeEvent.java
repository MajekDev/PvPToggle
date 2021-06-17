package dev.majek.pvptoggle.util;

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
