package dev.majek.pvptoggle.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class DataHandler {

  private final Map<UUID, User> userMap;

  public DataHandler() {
    userMap = new HashMap<>();
  }

  public Collection<User> getAllUsers() {
    return userMap.values();
  }

  public void addUser(@NotNull User user) {
    userMap.put(user.id(), user);
  }

  @NotNull
  public User getUser(@NotNull Player player) {
    if (userMap.containsKey(player.getUniqueId())) {
      return userMap.get(player.getUniqueId());
    } else {
      return new User(player);
    }
  }

  @Nullable
  public User getUser(@NotNull String name) {
    return getAllUsers().stream().filter(user -> user.username().equalsIgnoreCase(name))
        .collect(Collectors.toList()).get(0);
  }

  public User getUser(@NotNull UUID uuid) {
    return userMap.get(uuid);
  }
}
