package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.hooks.GriefPrevention;
import dev.majek.pvptoggle.hooks.WorldGuard;
import dev.majek.pvptoggle.util.TabCompleterBase;
import dev.majek.pvptoggle.util.TimeInterval;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandPvP implements CommandExecutor, TabCompleter {

    FileConfiguration config = PvPToggle.config;
    public static Map<Player, Long> cooldownMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("pvp")) {

            // The console is trying to change a player's pvp status
            if (!(sender instanceof Player)) {
                if (args.length == 2) {
                    Boolean toggle = null;
                    if (args[0].equalsIgnoreCase("on"))
                        toggle = true;
                    else if (args[0].equalsIgnoreCase("off"))
                        toggle = false;
                    UUID uuid = Bukkit.getPlayerUniqueId(args[1]);
                    if (uuid == null || toggle == null) {
                        sender.sendMessage(ChatColor.RED + "Console usage: /pvp <on|off> <player>"); return true;
                    }
                    OfflinePlayer person = Bukkit.getOfflinePlayer(uuid);
                    sender.sendMessage(PvPToggle.format((config.getString("pvp-toggle-other") + "")
                            .replace("%toggle%", toggle ? config.getString("forced-on")
                                    + "" : config.getString("forced-off") + "")
                            .replace("%player%", person.getName())));
                    if (person.isOnline()) {
                        Player onlinePerson = person.getPlayer();
                        onlinePerson.sendMessage(PvPToggle.format(config.getString(PvPToggle.getCore()
                                .hasPvPOn(onlinePerson) ? "pvp-enabled" : "pvp-disabled")));
                    }
                    PvPToggle.getCore().setStatus(uuid, toggle);
                } else {
                    sender.sendMessage(ChatColor.RED + "Console usage: /pvp <on|off> <player>"); return true;
                }
                return true;
            }

            // A player is trying to change a player's pvp status
            Player player = (Player) sender;

            // Make sure the player has the proper permission
            if (config.getBoolean("use-permissions") && !player.hasPermission("pvptoggle.use")) {
                player.sendMessage(PvPToggle.format(config.getString("no-permission"))); return true;
            }

            if (config.getInt("pvp-cooldown") > 0 && cooldownMap.containsKey(player)) {
                long timeSinceLast = (System.currentTimeMillis() - cooldownMap.get(player));
                if ((timeSinceLast / 1000L) < config.getInt("pvp-cooldown", 60)) {
                    long useIn = (config.getInt("pvp-cooldown") * 1000L) - timeSinceLast;
                    player.sendMessage(PvPToggle.format((config.getString("on-cooldown") + "")
                            .replace("%cooldown%", TimeInterval.formatTime(useIn, true))));
                    return true;
                }

            }

            // Check if a player is in a region that doesn't allow pvp toggling
            if (playerIsInRegion(player) && getRegionToggle(player) != null) {
                player.sendMessage(PvPToggle.format((config.getString("region-deny") + "")
                        .replace("%noun%", args.length > 1 ? (config.getString("player-is") + "")
                                .replace("%player%", args[1]) : config.getString("you-are") + "")
                        .replace("%toggle%", getRegionToggle(player) ? config.getString("forced-on")
                                + "" : config.getString("forced-off") + "")));
                return true;
            }

            boolean statusOther = false;

                // Set the player's own pvp status to the opposite of what it was
            if (args.length == 0) {
                player.sendMessage(PvPToggle.format(config.getString(!PvPToggle.getCore().hasPvPOn(player) ?
                        "pvp-enabled" : "pvp-disabled")));
                PvPToggle.getCore().setStatus(player.getUniqueId(), !PvPToggle.getCore().hasPvPOn(player));

                // Set the player's own pvp status to the specified toggle
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("on")) {
                    PvPToggle.getCore().setStatus(player.getUniqueId(), true);
                    player.sendMessage(PvPToggle.format(config.getString("pvp-enabled")));
                } else if (args[0].equalsIgnoreCase("off")) {
                    PvPToggle.getCore().setStatus(player.getUniqueId(), false);
                    player.sendMessage(PvPToggle.format(config.getString("pvp-disabled")));
                } else {
                    player.sendMessage(PvPToggle.format(config.getString("unknown-command"))); return true;
                }

                // Set a target player's pvp status to the specified toggle
            } else if (args.length == 2) {
                // Make sure player has permission to change other player's pvp status
                if (!player.hasPermission("pvptoggle.others")) {
                    player.sendMessage(PvPToggle.format(config.getString("no-permission"))); return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                Boolean toggle = null;
                if (args[0].equalsIgnoreCase("on"))
                    toggle = true;
                else if (args[0].equalsIgnoreCase("off"))
                    toggle = false;

                // Unknown player or incorrect syntax
                if (target == null) {
                    player.sendMessage(PvPToggle.format((config.getString("unknown-player") + "")
                            .replace("%player%", args[1]))); return true;
                }
                if (toggle == null) {
                    player.sendMessage(PvPToggle.format(config.getString("unknown-command"))); return true;
                }

                statusOther = true;

                // Set the player's pvp status and notify both players
                PvPToggle.getCore().setStatus(target.getUniqueId(), toggle);
                player.sendMessage(PvPToggle.format((config.getString("pvp-toggle-other") + "")
                        .replace("%toggle%", toggle ? config.getString("forced-on")
                                + "" : config.getString("forced-off") + "")
                        .replace("%player%", target.getName())));
                target.sendMessage(PvPToggle.format(config.getString(toggle ?
                        "pvp-enabled" : "pvp-disabled")));

                if (PvPToggle.debug)
                    target.sendMessage(player.getName() + " has updated your pvp status to " + toggle);
            }

            // Put the player in the cooldown hashmap if they set their own
            if (!statusOther)
                cooldownMap.put(player, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1)
            return TabCompleterBase.filterStartingWith(args[0], Arrays.asList("on", "off"));
        else if (args.length == 2)
            return TabCompleterBase.filterStartingWith(args[1], sender.hasPermission("pvptoggle.others") ?
                    TabCompleterBase.getOnlinePlayers(args[1]) : Collections.emptyList());
        else
            return Collections.emptyList();
    }

    /**
     * This is so we don't run into issues with a player being in multiple regions from different plugins
     */
    String regionPluginFound = "";

    /**
     * Check if a player is in a region
     * @param player the player to check
     * @return true if in region, false if not
     */
    public boolean playerIsInRegion(Player player) {
        if (PvPToggle.hasWorldGuard) {
            regionPluginFound = "WorldGuard";
            return WorldGuard.isInRegion(player);
        }
        if (PvPToggle.hasGriefPrevention) {
            regionPluginFound = "GriefPrevention";
            return GriefPrevention.playerInRegion(player.getLocation());
        }
        return false;
    }

    /**
     * Check if the region a player is in specifies a pvp toggle
     * @param player the player who's location to we want to check
     * @return true: pvp forced on | false: pvp forced off | null: not specified
     */
    public Boolean getRegionToggle(Player player) {
        switch (regionPluginFound) {
            case "WorldGuard":
                return WorldGuard.getRegionToggle(player);
            case "GriefPrevention":
                return GriefPrevention.getRegionToggle(player.getLocation());
            default:
                return null;
        }
    }
}
