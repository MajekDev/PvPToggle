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

  private final JsonConfig jsonConfig;

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
        PvPToggle.userHandler().addUser(new User(entry.getValue().getAsJsonObject()));
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
    PvPToggle.userHandler().addUser(user);
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
