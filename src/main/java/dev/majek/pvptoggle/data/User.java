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
  private boolean inRegion;

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
    this.inRegion = false;
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
    this.inRegion = json.get("inRegion") != null && json.get("inRegion").getAsBoolean();
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
   * @return Whether the user can change their status.
   */
  public boolean pvpBlocked() {
    return pvpBlocked;
  }

  /**
   * Set whether the user can change their status.
   *
   * @param pvpBlocked Whether status can be changed.
   */
  public User pvpBlocked(boolean pvpBlocked) {
    this.pvpBlocked = pvpBlocked;
    return this;
  }

  public boolean inRegion() {
    return inRegion;
  }

  public void inRegion(boolean inRegion) {
    this.inRegion = inRegion;
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
    json.addProperty("inRegion", inRegion);
    return json;
  }
}