package dev.majek.pvptoggle.hooks;

import dev.majek.pvptoggle.PvPToggle;
import me.angeschossen.lands.api.integration.LandsIntegration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Lands implements Listener {

  private static LandsIntegration landsIntegration;

  public Lands() {
    Lands.landsIntegration = new LandsIntegration(PvPToggle.core());
  }

  public static boolean canPvP(Player attacker, Player target) {
    return landsIntegration.canPvP(attacker, target, target.getLocation(), false, false);
  }
}
