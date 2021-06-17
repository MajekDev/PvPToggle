package dev.majek.pvptoggle.events;

import dev.majek.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (PvPToggle.usingMySQL) {
      PvPToggle.getCore().setStatus(player.getUniqueId(), PvPToggle.getMySQL().getStatus(player.getUniqueId()));
    }
    // Set the player's pvp status if the player has never joined
    // The boolean value for pvp status is configurable in the config.yml
    if (PvPToggle.getCore().isNotInHashmap(player)) {
      if (PvPToggle.config.getBoolean("use-permissions")) {
        if (player.hasPermission("pvptoggle.use"))
          PvPToggle.getCore().setStatus(player.getUniqueId(), PvPToggle.config.getBoolean("default-pvp"));
      } else
        PvPToggle.getCore().setStatus(player.getUniqueId(), PvPToggle.config.getBoolean("default-pvp"));
    }
    // Turn pvp off based on config option
    if (PvPToggle.config.getBoolean("off-on-join"))
      PvPToggle.getCore().setStatus(player.getUniqueId(), false);
  }
}
