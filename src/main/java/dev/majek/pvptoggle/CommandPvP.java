package dev.majek.pvptoggle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CommandPvP implements CommandExecutor, TabCompleter {

    FileConfiguration config = PvPToggle.config;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("pvp")) {
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
                    String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                    PvPToggle.getInstance().setStatus(uuid, toggle);
                    sender.sendMessage(ChatColor.GREEN + PvPToggle.format((toggle ? config.getString("pvp-enabled-other")
                            + "" : config.getString("pvp-disabled-other") + "")
                            .replace("%player%", playerName == null ? "null" : playerName)));
                } else {
                    sender.sendMessage(ChatColor.RED + "Console usage: /pvp <on|off> <player>"); return true;
                }
                return true;
            }
            Player player = (Player) sender;
            if (args.length == 0) {
                PvPToggle.getInstance().setStatus(player.getUniqueId(), !PvPToggle.getInstance().hasPvPOn(player));
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }

    public boolean regionCheck(Location location) {
        return false;
    }
}
