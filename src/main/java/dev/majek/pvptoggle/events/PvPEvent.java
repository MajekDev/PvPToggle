package dev.majek.pvptoggle.events;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.hooks.Lands;
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
        if (!(event.getEntity() instanceof Player))
            return;

        // If the player doesn't have permission we're not helping them :)
        if (PvPToggle.config.getBoolean("use-permissions") && !event.getEntity().hasPermission("pvptoggle.use"))
            return;

        // Handle damage from Citizens NPCs
        if (event.getDamager().hasMetadata("NPC"))
            return;

        // Handle damage from projectiles
        Player damager = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
        if (damager == null && event.getDamager() instanceof Projectile) {
            Projectile p = (Projectile) event.getDamager();
            damager = p.getShooter() instanceof Player ? (Player) p.getShooter() : null;
        }

        // Ignore if the player is damaging themselves or an entity shot a projectile at them
        if (damager == null || damager == event.getEntity())
            return;
        Player attacked = (Player) event.getEntity();

        // Cancel event and send messages
        if (PvPToggle.hasLands) {
            if (!Lands.canPvP(damager, attacked)) {
                damager.sendMessage(PvPToggle.format((PvPToggle.config.getString("region-deny") + "")
                        .replace("%noun%", PvPToggle.config.getString("you-are") + "")
                        .replace("%toggle%", PvPToggle.config.getString("forced-off") + "")));
                event.setCancelled(true);
                event.getEntity().setFireTicks(-1);
                return;
            }
        }
        if (!PvPToggle.getCore().hasPvPOn(damager)) {
            damager.sendMessage(PvPToggle.format(PvPToggle.config.getString("no-pvp")));
            event.setCancelled(true);
            event.getEntity().setFireTicks(-1);
        } else if (!PvPToggle.getCore().hasPvPOn(attacked)) {
            damager.sendMessage(PvPToggle.format((PvPToggle.config.getString("other-pvp") + "")
                    .replace("%player%", attacked.getDisplayName())));
            event.setCancelled(true);
            event.getEntity().setFireTicks(-1);
        }
    }
}
