package dev.majek.pvptoggle.storage;

import com.google.gson.JsonElement;
import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.JsonConfig;
import dev.majek.pvptoggle.data.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;

public class JsonStorage implements StorageMethod {

  JsonConfig jsonConfig;

  public JsonStorage() {
    jsonConfig = new JsonConfig(PvPToggle.core().getDataFolder(), "pvp.json");
    try {
      jsonConfig.createConfig();
    } catch (FileNotFoundException e) {
      PvPToggle.core().getLogger().severe("Unable to create pvp.json storage file!");
      e.printStackTrace();
    }
  }

  @Override
  public void loadAllUsers() {
    try {
      for (Map.Entry<String, JsonElement> entry : jsonConfig.toJsonObject().entrySet()) {
        PvPToggle.dataHandler().addUser(new User(entry.getValue().getAsJsonObject()));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addUser(@NotNull User user) {
    try {
      jsonConfig.putInJsonObject(user.id().toString(), user.toJson());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    PvPToggle.dataHandler().addUser(user);
  }

  @Override
  @Nullable
  public User getUser(@NotNull UUID uuid) {
    try {
      return new User(jsonConfig.toJsonObject().get(uuid.toString()).getAsJsonObject());
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public void updateUser(@NotNull User user) {
    addUser(user);
  }

  @Override
  public void removeUser(@NotNull UUID uuid) {
    try {
      jsonConfig.removeFromJsonObject(uuid.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}