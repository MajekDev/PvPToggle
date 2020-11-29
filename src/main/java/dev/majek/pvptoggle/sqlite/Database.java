package dev.majek.pvptoggle.sqlite;

import dev.majek.pvptoggle.PvPToggle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Main class for SQLite functions
 * Can you tell I followed different tutorials for MySQL and SQLite xD
 */
public abstract class Database {

    PvPToggle plugin;
    Connection connection;
    public String table = "pvp_data";

    public Database(PvPToggle instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        connection = getSQLConnection();
    }

    /**
     * Close connection
     * @param ps prepared statement
     * @param rs result set
     */
    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }

    /**
     * Clear the entire data table
     */
    public void clearTable() {
        Connection conn;
        PreparedStatement ps;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM pvp_data");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
    }

    /**
     * Updata a player's status in the data table
     * @param uuid the player's unique id
     */
    public void updatePlayer(UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        // Delete the current entry for the party
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM pvp_data WHERE playerUUID = '"+uuid.toString()+"';");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        addPlayer(uuid, PvPToggle.getCore().hasPvPOn(uuid));
    }

    /**
     * Add a player to the data table
     * @param uuid the player's unique id
     * @param pvp the player's pvp status
     */
    public void addPlayer(UUID uuid, boolean pvp) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO " + table + " (playerUUID,pvp) VALUES(?,?)");
            ps.setString(1, uuid.toString());
            ps.setInt(2, pvp ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }

    }

    List<String> playerUUIDS = new ArrayList<>();

    /**
     * Get all of the players in the data table
     */
    public void getPlayers() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table);
            rs = ps.executeQuery();
            while (rs.next()) {
                playerUUIDS.add(rs.getString("playerUUID"));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    /**
     * Get the player's pvp statuses
     */
    public void getPvPStatuses() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        String uuid = null; int pvp = 0;
        for (String playerUUID : playerUUIDS) {
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE playerUUID = '"+playerUUID+"';");
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getString("playerUUID").equalsIgnoreCase(playerUUID)) {
                        uuid = rs.getString("name");
                        pvp = rs.getInt("pvp");
                    }
                }
                PvPToggle.getCore().setStatus(UUID.fromString(uuid), pvp != 0);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            } finally {
                try {
                    if (ps != null)
                        ps.close();
                    if (conn != null)
                        conn.close();
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
                }
            }
        }
    }

}
