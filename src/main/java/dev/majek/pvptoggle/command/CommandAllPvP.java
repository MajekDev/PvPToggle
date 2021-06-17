package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandAllPvP implements TabExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    if (args.length == 0)
      return false;
    boolean status;
    if (args[0].equalsIgnoreCase("on"))
      status = true;
    else if (args[0].equalsIgnoreCase("off"))
      status = false;
    else
      return false;
    for (Player player : Bukkit.getOnlinePlayers()) {
      PvPToggle.getCore().setStatus(player.getUniqueId(), status);
      player.sendMessage(PvPToggle.format(PvPToggle.getCore().getConfig()
          .getString(status ? "pvp-enabled" : "pvp-disabled")));
    }
    sender.sendMessage(PvPToggle.format((PvPToggle.getCore().getConfig().getString("pvp-toggle-all") + "")
        .replace("%toggle%", String.valueOf(status))));
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                                    @NotNull String[] args) {
    if (args.length == 1)
      return Arrays.asList("on", "off");
    else
      return Collections.emptyList();
  }
}
