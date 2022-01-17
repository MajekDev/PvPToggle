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
package dev.majek.pvptoggle.message;

import dev.majek.chattools.MiniMessageWrapper;
import dev.majek.pvptoggle.PvPToggle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface Message {

  Args1<Boolean> PVP_CHANGED = (on) -> on ? configString("pvp-enabled", "&6PvP is now: &aEnabled")
      : configString("pvp-disabled", "&6PvP is now: &cDisabled");

  Args0 PVP_ENABLED = () -> configString("pvp-enabled", "&6PvP is now: &aEnabled");

  Args0 PVP_DISABLED = () -> configString("pvp-disabled", "&6PvP is now: &cDisabled");

  Args0 NO_PVP = () -> configString("no-pvp", "&6PvP is disabled. Use &7/pvp &6to turn it on.");

  Args1<Player> OTHER_PVP = (player) -> configStringPlaceholders("other-pvp", "&b%player% &6has PvP disabled!", player)
      .replaceText(TextReplacementConfig.builder().match("%player%").replacement(player.getName()).build());

  Args1<String> UNKNOWN_PLAYER = (name) -> configString("unknown-player", "&cUnable to locate player %player%.")
      .replaceText(TextReplacementConfig.builder().match("%player%").replacement(name).build());

  Args0 NO_PERMISSION = () -> configString("no-permission", "&cYou do not have permission to do that!");

  Args1<String> ON_COOLDOWN = (cooldown) -> configString("on-cooldown", "&cYou may use this command again in %cooldown%.")
      .replaceText(TextReplacementConfig.builder().match("%cooldown%").replacement(cooldown).build());

  Args0 PLUGIN_RELOADED = () -> configString("plugin-reloaded", "&6PvPToggle reloaded.");

  Args2<Boolean, Player> DISABLED_WORLD = (isSelf, player) -> configString("disabled-world", "&c%noun% in a world where this command is disabled.")
      .replaceText(TextReplacementConfig.builder().match("%noun%").replacement(
          isSelf ? configString("you-are", "You are") : configString("player-is", "%player% is")
              .replaceText(TextReplacementConfig.builder().match("%player%").replacement(player.getName()).build())
      ).build());

  Args2<Boolean, Player> PVP_TOGGLE_OTHER = (on, target) ->
      configStringPlaceholders("pvp-toggle-other", "&6PvP is now %toggle% for &b%player%&6.", target)
          .replaceText(TextReplacementConfig.builder().match("%toggle%").replacement(
              on ? configString("forced-on", "on") : configString("forced-off", "off")
          ).build())
          .replaceText(TextReplacementConfig.builder().match("%player%").replacement(target.getName()).build());

  Args1<Boolean> PVP_TOGGLE_ALL = (on) -> configString("pvp-toggle-all", "&6PvP is now %toggle% for everyone.")
      .replaceText(TextReplacementConfig.builder().match("%toggle%").replacement(
          on ? configString("forced-on", "on") : configString("forced-off", "off")
      ).build());

  Args1<Boolean> PVP_BLOCK_SET = (on) -> configString("pvp-block-set", "&6Global PvP blocking is now set to %toggle%.")
      .replaceText(TextReplacementConfig.builder().match("%toggle%").replacement(
          on ? configString("forced-on", "on") : configString("forced-off", "off")
      ).build());

  Args0 PVP_BLOCKED = () -> configString("pvp-blocked", "&6Toggling PvP is blocked.");

  Args3<Boolean, Boolean, Player> REGION_DENY = (on, isSelf, player) -> configString("region-deny", "&c%noun% in a region with PvP forced %toggle%.")
      .replaceText(TextReplacementConfig.builder().match("%toggle%").replacement(
          on ? configString("forced-on", "on") : configString("forced-off", "off")
      ).build())
      .replaceText(TextReplacementConfig.builder().match("%noun%").replacement(
          isSelf ? configString("you-are", "You are") : configString("player-is", "%player% is")
              .replaceText(TextReplacementConfig.builder().match("%player%").replacement(player.getName()).build())
      ).build());

  Args1<Boolean> REGION_ENTER = (on) -> configString("region-enter", "&cYou have entered a region where PvP is forced %toggle%.")
      .replaceText(TextReplacementConfig.builder().match("%toggle%").replacement(
          on ? configString("forced-on", "on") : configString("forced-off", "off")
      ).build());

  Args1<Boolean> YOUR_PVP = (on) -> configString("your-pvp", "&6Your PvP is currently %toggle%. Use /pvp <on|off> to change it.")
      .replaceText(TextReplacementConfig.builder().match("%toggle%").replacement(
          on ? configString("forced-on", "on") : configString("forced-off", "off")
      ).build());

  /**
   * A message that has no arguments that need to be replaced.
   */
  interface Args0 {
    Component build();

    default void send(CommandSender sender) {
      PvPToggle.core().sendMessage(sender, build());
    }
  }

  /**
   * A message that has one argument that needs to be replaced.
   */
  interface Args1<A0> {
    Component build(A0 arg0);

    default void send(CommandSender sender, A0 arg0) {
      PvPToggle.core().sendMessage(sender, build(arg0));
    }
  }

  /**
   * A message that has two arguments that need to be replaced.
   */
  interface Args2<A0, A1> {
    Component build(A0 arg0, A1 arg1);

    default void send(CommandSender sender, A0 arg0, A1 arg1) {
      PvPToggle.core().sendMessage(sender, build(arg0, arg1));
    }
  }

  /**
   * A message that has three arguments that need to be replaced.
   */
  interface Args3<A0, A1, A2> {
    Component build(A0 arg0, A1 arg1, A2 arg2);

    default void send(CommandSender sender, A0 arg0, A1 arg1, A2 arg2) {
      PvPToggle.core().sendMessage(sender, build(arg0, arg1, arg2));
    }
  }

  /**
   * Get a string from the config, apply placeholders from PlaceholderAPI,
   * and parse it into a Component with MiniMessage.
   *
   * @param path The path to the string.
   * @param def The default if the path returns null.
   * @param player The player for placeholders.
   * @return Formatted component.
   */
  static Component configStringPlaceholders(String path, String def, Player player) {
    return MiniMessageWrapper.legacy().mmParse(
        PvPToggle.hookManager().applyPlaceHolders(player, PvPToggle.core().getConfig().getString(path, def))
    );
  }

  /**
   * Get a string from the config and parse it into a Component with MiniMessage.
   *
   * @param path The path to the string.
   * @param def The default if the path returns null.
   * @return Formatted component.
   */
  static Component configString(String path, String def) {
    return MiniMessageWrapper.legacy().mmParse(PvPToggle.core().getConfig().getString(path, def));
  }
}
