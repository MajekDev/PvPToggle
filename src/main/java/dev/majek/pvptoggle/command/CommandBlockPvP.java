package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandBlockPvP implements TabExecutor {

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                           @NotNull String[] args) {
    if (args.length == 0)
      return false;
    boolean status;
    if (args[0].equalsIgnoreCase("true"))
      status = true;
    else if (args[0].equalsIgnoreCase("false"))
      status = false;
    else
      return false;
    PvPToggle.blockPvp = status;
    sender.sendMessage(PvPToggle.format((PvPToggle.getCore().getConfig().getString("pvp-block-set") + "")
        .replace("%toggle%", String.valueOf(status))));
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1)
      return Arrays.asList("true", "false");
    else
      return Collections.emptyList();
  }
}
