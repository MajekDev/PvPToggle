package dev.majek.pvptoggle.data;

import com.google.gson.JsonObject;
import dev.majek.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A user of the plugin.
 */
public class User {

  private final UUID uuid;
  private String username;
  private boolean pvpStatus;
  private boolean pvpBlocked;

  /**
   * Create a new user from a {@link Player} who has just joined.
   *
   * @param player The player.
   */
  public User(@NotNull Player player) {
    this.uuid = player.getUniqueId();
    this.username = player.getName();
    this.pvpStatus = PvPToggle.core().getConfig().getBoolean("default-pvp", false);
    this.pvpBlocked = false;
  }

  /**
   * Create a new user from Json storage.
   *
   * @param json User info stored in Json.
   */
  public User(@NotNull JsonObject json) {
    this.uuid = UUID.fromString(json.get("uuid").getAsString());
    this.username = json.get("username").getAsString();
    this.pvpStatus = json.get("pvpStatus").getAsBoolean();
    this.pvpBlocked = json.get("pvpBlocked").getAsBoolean();
  }

  /**
   * Get the user's unique id.
   *
   * @return Unique id.
   */
  public UUID id() {
    return uuid;
  }

  /**
   * Get the user's username.
   *
   * @return Username.
   */
  public String username() {
    return username;
  }

  /**
   * Set the user's username.
   *
   * @param username Username.
   */
  public User username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get the user's pvp status.
   *
   * @return PvP status.
   */
  public boolean pvpStatus() {
    return pvpStatus;
  }

  /**
   * Set the user's pvp status.
   *
   * @param pvpStatus PvP status.
   */
  public User pvpStatus(boolean pvpStatus) {
    this.pvpStatus = pvpStatus;
    return this;
  }

  /**
   * Check if the user is blocked from changing their pvp status.
   *
   * @return Whether or not the user can change their status.
   */
  public boolean pvpBlocked() {
    return pvpBlocked;
  }

  /**
   * Set whether or not the user can change their status.
   *
   * @param pvpBlocked Whether or not status can be changed.
   */
  public User pvpBlocked(boolean pvpBlocked) {
    this.pvpBlocked = pvpBlocked;
    return this;
  }

  public void updateUser() {
    PvPToggle.storageMethod().updateUser(this);
  }

  /**
   * Get the user info represented in Json for storage.
   *
   * @return User info.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("uuid", uuid.toString());
    json.addProperty("username",  username);
    json.addProperty("pvpStatus", pvpStatus);
    json.addProperty("pvpBlocked", pvpBlocked);
    return json;
  }
}