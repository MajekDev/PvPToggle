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

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserHandler {

  private final Map<UUID, User> userMap;

  public UserHandler() {
    userMap = new HashMap<>();
  }

  public Collection<User> getAllUsers() {
    return userMap.values();
  }

  public void addUser(@NotNull User user) {
    userMap.put(user.id(), user);
  }

  public @NotNull User getUser(@NotNull Player player) {
    if (userMap.containsKey(player.getUniqueId())) {
      return userMap.get(player.getUniqueId());
    } else {
      User user = new User(player);
      addUser(user);
      return user;
    }
  }

  public @Nullable User getUser(@NotNull String name) {
    return getAllUsers().stream().filter(user -> user.username().equalsIgnoreCase(name))
        .collect(Collectors.toList()).get(0);
  }

  public User getUser(@NotNull UUID uuid) {
    return userMap.get(uuid);
  }
}
