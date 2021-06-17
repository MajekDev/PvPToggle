package dev.majek.pvptoggle.data;

import dev.majek.pvptoggle.PvPToggle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main class for MySQL functions
 */
public class SQLGetter {

  private final PvPToggle plugin;

  public SQLGetter(PvPToggle plugin) {
    this.plugin = plugin;
  }

  /**
   * Create the pvp table in the MySQL database
   */
  public void createTable() {
    PreparedStatement ps;
    try {
      ps = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS pvp_data (" +
          "playerUUID varchar(64) NOT NULL," +
          "pvp int(2) NOT NULL," +
          "PRIMARY KEY (playerUUID));");
      ps.executeUpdate();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Update a player's pvp status in the table
   *
   * @param uuid the player's unique id
   */
  public void updateStatus(UUID uuid) {
    try {
      if (exists(uuid)) {
        // If it exists, delete it first before adding in the updated status
        PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("DELETE FROM pvp_data " +
            "WHERE playerUUID=?");
        ps.setString(1, uuid.toString());
        ps.executeUpdate();
      }
      PreparedStatement ps2 = plugin.SQL.getConnection().prepareStatement("INSERT IGNORE INTO pvp_data " +
          "(playerUUID,pvp) VALUES (?,?)");
      ps2.setString(1, uuid.toString());
      ps2.setInt(2, PvPToggle.getCore().hasPvPOn(uuid) ? 1 : 0);
      ps2.executeUpdate();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Check if a player is already in the table
   *
   * @param uuid the player's unique id
   * @return true -> exists | false -> doesn't
   */
  public boolean exists(UUID uuid) {
    try {
      PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM pvp_data WHERE playerUUID=?");
      ps.setString(1, uuid.toString());
      ResultSet resultSet = ps.executeQuery();
      return resultSet.next();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Get the player's pvp status from the database
   *
   * @param uuid the player's unique id
   * @return pvp status
   */
  public boolean getStatus(UUID uuid) {
    try {
      PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT pvp FROM pvp_data WHERE playerUUID=?");
      ps.setString(1, uuid.toString());
      ResultSet resultSet = ps.executeQuery();
      if (resultSet.next()) {
        return resultSet.getInt("pvp") == 1;
      }
      ps.executeUpdate();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    // Return default pvp if something went wrong
    return PvPToggle.config.getBoolean("default-pvp");
  }

  /**
   * Get all players statuses and add them to the hashmap
   */
  public void getAllStatuses() {
    List<UUID> playerUUIDS = new ArrayList<>();
    try {
      PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM pvp_data");
      ResultSet resultSet = ps.executeQuery();
      while (resultSet.next()) {
        playerUUIDS.add(UUID.fromString(resultSet.getString("playerUUID")));
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    for (UUID playerUUID : playerUUIDS) {
      try {
        PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM pvp_data" +
            " WHERE playerUUID = '" + playerUUID + "';");
        ResultSet resultSet = ps.executeQuery();
        UUID uuid = null;
        int pvp = 0;
        while (resultSet.next()) {
          if (resultSet.getString("playerUUID").equalsIgnoreCase(playerUUID.toString())) {
            uuid = UUID.fromString(resultSet.getString("name"));
            pvp = resultSet.getInt("pvp");
          }
        }
        PvPToggle.getCore().setStatus(uuid, pvp != 0);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Remove a player from the table
   *
   * @param uuid the player's unique id
   */
  public void removeStatus(UUID uuid) {
    try {
      PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("DELETE FROM pvp_data WHERE playerUUID=?");
      ps.setString(1, uuid.toString());
      ps.executeUpdate();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

}
