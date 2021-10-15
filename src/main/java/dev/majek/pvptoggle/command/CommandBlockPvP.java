/*
 * This file is part of PvPToggle, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Majekdor
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.majek.pvptoggle.command;

import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.User;
import dev.majek.pvptoggle.message.Message;
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
    if (args[1].equalsIgnoreCase("all")) {
      PvPToggle.userHandler().getAllUsers().forEach(user -> user.pvpBlocked(status).updateUser());
    } else {
      User user = PvPToggle.userHandler().getUser(args[1]);
      if (user == null) {
        Message.UNKNOWN_PLAYER.send(sender, args[1]);
        return true;
      }
      user.pvpBlocked(status).updateUser();
    }

    Message.PVP_BLOCK_SET.send(sender, !status);
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
        List<String> options = PvPToggle.userHandler().getAllUsers().stream().filter(User::pvpBlocked)
            .map(User::username).collect(Collectors.toList());
        options.add("all");
        return TabCompleterBase.filterStartingWith(args[1], options);
      }
    }
    return Collections.emptyList();
  }
}