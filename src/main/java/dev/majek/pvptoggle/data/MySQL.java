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
package dev.majek.pvptoggle.data;

import dev.majek.pvptoggle.PvPToggle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {

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
    String host = PvPToggle.config().getString("host");
    String port = PvPToggle.config().getString("port");
    String database = PvPToggle.config().getString("database");
    String username = PvPToggle.config().getString("username");
    String password = PvPToggle.config().getString("password");
    boolean useSSL = PvPToggle.config().getBoolean("use-ssl");
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
