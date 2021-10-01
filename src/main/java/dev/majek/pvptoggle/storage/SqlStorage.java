package dev.majek.pvptoggle.storage;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import dev.majek.pvptoggle.PvPToggle;
import dev.majek.pvptoggle.data.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqlStorage implements StorageMethod {

  @Override
  public void loadAllUsers() {
    try {
      PreparedStatement ps = PvPToggle.sql().getConnection().prepareStatement("SELECT * FROM pvp_data");
      ResultSet resultSet = ps.executeQuery();
      while (resultSet.next()) {
        User user = getUser(UUID.fromString(resultSet.getString("playerUUID")));
        if (user != null) {
          PvPToggle.dataHandler().addUser(user);
          PvPToggle.debug("Loaded user: " + user.username());
        }
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void addUser(@NotNull User user) {
    try {
      PreparedStatement ps = PvPToggle.sql().getConnection()
          .prepareStatement("INSERT INTO `pvp_data` (`playerUUID`, `json`) VALUES (?, ?);");
      ps.setString(1, user.id().toString());
      ps.setString(2, new Gson().toJson(user.toJson()));
      ps.executeUpdate();
      PvPToggle.debug("Added user " + user.username() + " to table");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    PvPToggle.dataHandler().addUser(user);
  }

  @Override
  @Nullable
  public User getUser(@NotNull UUID uuid) {
    try {
      PreparedStatement ps = PvPToggle.sql().getConnection()
          .prepareStatement("SELECT json FROM pvp_data WHERE playerUUID=?");
      ps.setString(1, uuid.toString());
      ResultSet resultSet = ps.executeQuery();
      if (resultSet.next()) {
        return new User(JsonParser.parseString(resultSet.getString("json")).getAsJsonObject());
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  @Override
  public void updateUser(@NotNull User user) {
    if (getUser(user.id()) == null) {
      addUser(user);
    }
    try {
      PreparedStatement update = PvPToggle.sql().getConnection()
          .prepareStatement("UPDATE pvp_data SET json=? WHERE playerUUID=?");
      update.setString(1, new Gson().toJson(user.toJson()));
      update.setString(2, user.id().toString());
      update.executeUpdate();
      PvPToggle.debug("Updated user " + user.username());
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    PvPToggle.dataHandler().addUser(user);
  }

  @Override
  public void removeUser(@NotNull UUID uuid) {
    try {
      PreparedStatement ps = PvPToggle.sql().getConnection().prepareStatement("DELETE FROM pvp_data WHERE playerUUID=?");
      ps.setString(1, uuid.toString());
      ps.executeUpdate();
      PvPToggle.debug("Removed user " + uuid + " from table");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
}