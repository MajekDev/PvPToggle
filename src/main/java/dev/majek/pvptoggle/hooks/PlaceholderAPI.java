package dev.majek.pvptoggle.hooks;

import dev.majek.pvptoggle.PvPToggle;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPI extends PlaceholderExpansion {

  private final PvPToggle plugin;

  public PlaceholderAPI(PvPToggle plugin) {
      this.plugin = plugin;
  }

  @Override
  public boolean canRegister() {
      return true;
  }

  @Override
  public boolean persist() {
      return true;
  }

  @Override
  public @NotNull String getAuthor() {
      return plugin.getDescription().getAuthors().get(0);
  }

  @Override
  public @NotNull String getIdentifier() {
      return plugin.getDescription().getName().toLowerCase();
  }

  @Override
  public @NotNull String getVersion() {
      return plugin.getDescription().getVersion();
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String identifier) {

    if (identifier.equalsIgnoreCase("status"))
      return PvPToggle.getCore().hasPvPOn(player.getUniqueId())
          ? PvPToggle.getCore().getConfig().getString("boolean-true", "true")
          : PvPToggle.getCore().getConfig().getString("boolean-false", "false");

    return null;
  }

}
