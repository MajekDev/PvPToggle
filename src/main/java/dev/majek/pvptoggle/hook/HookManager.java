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
package dev.majek.pvptoggle.hook;

import dev.majek.pvptoggle.PvPToggle;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class HookManager {

  private boolean papiHooked;
  private boolean worldGuardHooked;
  private boolean regionProtectionHooked; // Not working yet
  private boolean griefPreventionHooked;
  private boolean landsHooked;
  private boolean griefDefenderHooked; // Not working yet

  private final Set<RegionHook> regionHookSet;

  public HookManager() {
    papiHooked = false;
    worldGuardHooked = false;
    regionProtectionHooked = false;
    griefPreventionHooked = false;
    landsHooked = false;
    griefDefenderHooked = false;
    regionHookSet = new HashSet<>();
  }

  public void reload() {
    if (PvPToggle.core().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") &&
        PvPToggle.core().getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
      PvPToggle.log("Hooking into PlaceholderAPI...");
      papiHooked = true;
      new PapiHook(PvPToggle.core()).register();
    }
    if (PvPToggle.core().getServer().getPluginManager().isPluginEnabled("WorldGuard") &&
        PvPToggle.core().getServer().getPluginManager().getPlugin("WorldGuard") != null) {
      PvPToggle.log("Hooking into WorldGuard...");
      worldGuardHooked = true;
      WorldGuardHook worldGuardHook = new WorldGuardHook();
      PvPToggle.core().getServer().getPluginManager().registerEvents(worldGuardHook, PvPToggle.core());
      regionHookSet.add(worldGuardHook);
    }
    if (PvPToggle.core().getServer().getPluginManager().isPluginEnabled("RegionProtection") &&
        PvPToggle.core().getServer().getPluginManager().getPlugin("RegionProtection") != null) {
      //PvPToggle.log("Hooking into RegionProtection...");
      regionProtectionHooked = true;
    }
    if (PvPToggle.core().getServer().getPluginManager().isPluginEnabled("GriefPrevention") &&
        PvPToggle.core().getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
      PvPToggle.log("Hooking into GriefPrevention...");
      griefPreventionHooked = true;
      GriefPreventionHook griefPreventionHook = new GriefPreventionHook();
      PvPToggle.core().getServer().getPluginManager().registerEvents(griefPreventionHook, PvPToggle.core());
      regionHookSet.add(griefPreventionHook);
    }
    if (PvPToggle.core().getServer().getPluginManager().isPluginEnabled("Lands") &&
        PvPToggle.core().getServer().getPluginManager().getPlugin("Lands") != null) {
      PvPToggle.log("Hooking into Lands...");
      landsHooked = true;
    }
    if (PvPToggle.core().getServer().getPluginManager().isPluginEnabled("GriefDefender") &&
        PvPToggle.core().getServer().getPluginManager().getPlugin("GriefDefender") != null) {
      //PvPToggle.log("Hooking into GriefDefender...");
      griefDefenderHooked = true;
    }
  }

  public boolean isPapiHooked() {
    return papiHooked;
  }

  public boolean isWorldGuardHooked() {
    return worldGuardHooked;
  }

  public boolean isRegionProtectionHooked() {
    return regionProtectionHooked;
  }

  public boolean isGriefPreventionHooked() {
    return griefPreventionHooked;
  }

  public boolean isLandsHooked() {
    return landsHooked;
  }

  public boolean isGriefDefenderHooked() {
    return griefDefenderHooked;
  }

  /**
   * Apply placeholders from PlaceholderAPI to a string.
   *
   * @param player The player referenced in the string.
   * @param string The string to search.
   * @return Formatted string.
   */
  public String applyPlaceHolders(Player player, String string) {
    return isPapiHooked() ? PapiHook.applyPlaceholders(player, string) : string;
  }

  public boolean isInRegion(@NotNull Player player) {
    for (RegionHook hook : regionHookSet) {
      if (hook.isInRegion(player)) {
        return true;
      }
    }
    return false;
  }

  public TriState getRegionToggle(@NotNull Player player) {
    for (RegionHook hook : regionHookSet) {
      if (hook.isInRegion(player)) {
        return hook.getRegionToggle(player);
      }
    }
    return TriState.NOT_SET;
  }
}