package dev.majek.pvptoggle.events;

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
      user = PvPToggle.dataHandler().getUser(player);
    }
    // Turn pvp off based on config option
    if (PvPToggle.config.getBoolean("off-on-join"))
      user.pvpStatus(false);
    PvPToggle.storageMethod().updateUser(user);
  }
}
