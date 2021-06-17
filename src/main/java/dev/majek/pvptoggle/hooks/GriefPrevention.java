package dev.majek.pvptoggle.hooks;

import dev.majek.pvptoggle.PvPToggle;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class GriefPrevention implements Listener {

  public static boolean playerInRegion(Location location) {
    Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore
        .getClaimAt(location, false, null);
    return claim != null;
  }

  public static Boolean getRegionToggle(Location location) {
    return isClaimPvPSafeZone(location) ? false : null;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    // Check if the player actually moved or just moved their head
    if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY()
        == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
    Player player = event.getPlayer();

    // Reset pvp status if the player is leaving a region
    if (PvPToggle.inRegion.contains(player.getUniqueId())) {
      if (!playerInRegion(event.getTo())) {
        PvPToggle.getCore().setStatus(player.getUniqueId(), PvPToggle.config.getBoolean("default-pvp"));
        PvPToggle.inRegion.remove(player.getUniqueId());
      }
    }

    check(event.getPlayer(), event.getTo());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    check(event.getPlayer(), event.getPlayer().getLocation());
  }

  public static boolean isClaimPvPSafeZone(Location location) {
    Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore
        .getClaimAt(location, false, null);
    return me.ryanhamshire.GriefPrevention.GriefPrevention.instance.claimIsPvPSafeZone(claim);
  }

  public void check(Player player, Location location) {
    if (isClaimPvPSafeZone(location)) {
      player.sendMessage(PvPToggle.format((PvPToggle.config.getString("region-enter") + "")
          .replace("%toggle%", PvPToggle.config.getString("forced-off") + "")));
      PvPToggle.getCore().setStatus(player.getUniqueId(), false);
      PvPToggle.inRegion.add(player.getUniqueId());
    }
  }
}
