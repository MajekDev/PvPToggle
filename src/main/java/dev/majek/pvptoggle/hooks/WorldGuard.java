package dev.majek.pvptoggle.hooks;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.majek.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

/**
 * WorldGuard region check and listener if the plugin is active
 */
public class WorldGuard implements Listener {

    public static WorldGuardPlugin worldGuardPlugin;
    public static WorldGuardPlugin getWorldGuard() {
        Plugin wgplugin = PvPToggle.instance.getServer().getPluginManager().getPlugin("WorldGuard");
        if (!(wgplugin instanceof WorldGuardPlugin)) {
            return null;
        }
        return (WorldGuardPlugin) wgplugin;
    }

    public static boolean isInRegion(Player player) {
        worldGuardPlugin = getWorldGuard();
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
        Location loc = localPlayer.getLocation();
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        return set.size() > 0; // Set size is 0 if player is in __global__ region
    }

    public static Boolean getRegionToggle(Player player) {
        worldGuardPlugin = getWorldGuard();
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
        Location loc = localPlayer.getLocation();
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        if (set.testState(localPlayer, Flags.PVP))
            return true;
        else if (!(set.testState(localPlayer, Flags.PVP)))
            return false;
        else
            return null;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check if the player actually moved or just moved their head
        if (event.getTo().getBlockX() == event.getFrom().getBlockX() && event.getTo().getBlockY()
                == event.getFrom().getBlockY() && event.getTo().getBlockZ() == event.getFrom().getBlockZ()) return;
        checkRegion(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkRegion(event.getPlayer(), false);
    }

    private void checkRegion(Player player, boolean fromPlayerMove) {
        // Get region and region information
        worldGuardPlugin = getWorldGuard();
        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(player);
        Location loc = localPlayer.getLocation();
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);

        // Check if the player enters the global region from a region
        if (set.size() == 0 && fromPlayerMove) {
            if (PvPToggle.inRegion.contains(player.getUniqueId())) {
                PvPToggle.getCore().setStatus(player.getUniqueId(), PvPToggle.config.getBoolean("default-pvp"));
                PvPToggle.inRegion.remove(player.getUniqueId());
            }

            // Check if the player enters into a region
        } else if (set.size() > 0 && !PvPToggle.inRegion.contains(player.getUniqueId())) {
            if (set.testState(localPlayer, Flags.PVP)) {
                player.sendMessage(PvPToggle.format((PvPToggle.config.getString("region-enter") + "")
                        .replace("%toggle%", PvPToggle.config.getString("forced-on") + "")));
                PvPToggle.getCore().setStatus(player.getUniqueId(), true);
                PvPToggle.inRegion.add(player.getUniqueId());
            } else if (!(set.testState(localPlayer, Flags.PVP))) {
                player.sendMessage(PvPToggle.format((PvPToggle.config.getString("region-enter") + "")
                        .replace("%toggle%", PvPToggle.config.getString("forced-off") + "")));
                PvPToggle.getCore().setStatus(player.getUniqueId(), false);
                PvPToggle.inRegion.add(player.getUniqueId());
            }
            // If the flag is neither on or off we don't need to do anything
        }
    }
}
