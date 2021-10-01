package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.User;
import dev.majek.pvptoggle.util.TabCompleterBase;
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
import java.util.stream.Collectors;

public class CommandBlockPvP implements TabExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    if (args.length < 2) {
      return false;
    }

    // Get the status of block pvp
    boolean status;
    if (args[0].equalsIgnoreCase("true"))
      status = true;
    else if (args[0].equalsIgnoreCase("false"))
      status = false;
    else
      return false;

    // Get a user or all
    PvPToggle.blockPvp = status;
    if (args[1].equalsIgnoreCase("all")) {
      PvPToggle.dataHandler().getAllUsers().forEach(user -> user.pvpBlocked(status).updateUser());
    } else {
      User user = PvPToggle.dataHandler().getUser(args[1]);
      if (user == null) {
        sender.sendMessage(PvPToggle.format(PvPToggle.core().getConfig().getString("unknown-player",
            "&cUnable to locate player %player%.").replace("%player%", args[1])));
        return true;
      }
      user.pvpBlocked(status).updateUser();
    }

    sender.sendMessage(PvPToggle.format((PvPToggle.core().getConfig().getString("pvp-block-set") + "")
        .replace("%toggle%", String.valueOf(status))));
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      return TabCompleterBase.filterStartingWith(args[0], Arrays.asList("true", "false"));
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("true")) {
        List<String> options = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        options.add("all");
        return TabCompleterBase.filterStartingWith(args[1], options);
      } else if (args[0].equalsIgnoreCase("false")) {
        List<String> options = PvPToggle.dataHandler().getAllUsers().stream().filter(User::pvpBlocked)
            .map(User::username).collect(Collectors.toList());
        options.add("all");
        return TabCompleterBase.filterStartingWith(args[1], options);
      }
    }
    return Collections.emptyList();
  }
}
