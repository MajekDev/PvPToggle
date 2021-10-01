package dev.majek.pvptoggle.storage;

import dev.majek.pvptoggle.data.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface StorageMethod {

  void loadAllUsers();

  void addUser(@NotNull User user);

  User getUser(@NotNull UUID uuid);

  void updateUser(@NotNull User user);

  void removeUser(@NotNull UUID uuid);

}