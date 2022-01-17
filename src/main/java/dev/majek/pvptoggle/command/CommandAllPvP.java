/*
 * This file is part of PvPToggle, licensed under the MIT License.
 *
 * Copyright (c) 2020-2022 Majekdor
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
import dev.majek.pvptoggle.message.Message;
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
    if (args.length == 0) {
      return false;
    }

    boolean status;
    if (args[0].equalsIgnoreCase("on")) {
      status = true;
    } else if (args[0].equalsIgnoreCase("off")) {
      status = false;
    } else {
      return false;
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      PvPToggle.core().setStatus(player.getUniqueId(), status);
      Message.PVP_CHANGED.send(player, status);
    }

    Message.PVP_TOGGLE_ALL.send(sender, status);
    return true;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                    @NotNull String label, @NotNull String[] args) {
    if (args.length == 1) {
      return Arrays.asList("on", "off");
    } else {
      return Collections.emptyList();
    }
  }
}
