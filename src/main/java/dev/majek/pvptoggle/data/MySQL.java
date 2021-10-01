package dev.majek.pvptoggle.data;

import dev.majek.pvptoggle.PvPToggle;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {

  FileConfiguration c = PvPToggle.core().getConfig();
  private final String host = c.getString("host");
  private final String port = c.getString("port");
  private final String database = c.getString("database");
  private final String username = c.getString("username");
  private final String password = c.getString("password");
  private final boolean useSSL = c.getBoolean("use-ssl");

  private Connection connection;

  public boolean isConnected() {
    return (connection != null);
  }

  /**
   * Try to connect to the database
   *
   * @throws SQLException if the connection fails
   */
  public void connect() throws SQLException {
    if (!isConnected()) {
      connection = DriverManager.getConnection("jdbc:mysql://" +
              host + ":" + port + "/" + database + "?useSSL=" + useSSL,
          username, password);
    }
  }

  /**
   * Disconnect from the database
   */
  public void disconnect() {
    if (isConnected()) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Get the connection to the database
   *
   * @return connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * Create the pvp table in the MySQL database
   */
  public void createTable() {
    PreparedStatement ps;
    try {
      ps = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS pvp_data (" +
          "playerUUID varchar(64) NOT NULL," +
          "json varchar(1000) NOT NULL," +
          "PRIMARY KEY (playerUUID));");
      ps.executeUpdate();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
}