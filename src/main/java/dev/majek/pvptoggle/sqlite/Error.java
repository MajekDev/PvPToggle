package dev.majek.pvptoggle.sqlite;

import dev.majek.pvptoggle.PvPToggle;

import java.util.logging.Level;

public class Error {
    public static void execute(PvPToggle plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(PvPToggle plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
