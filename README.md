# PvPToggle v2

PvPToggle is a plugin that allows players to use a simple command (/pvp) to change their "PvP status." When a player has PvP toggled off they won't be able to take damage
from other players or deal any damage to other players. Damage from mobs, explosions, lava, etc. will not be affected by your PvP status. 

## Commands and Permissions
Unless changed in the configuration file, all players will be able to use `/pvp`. If `use-permissions` is enabled in the config file, only players with the permission
`pvptoggle.use` will be able to access `/pvp`. If this is enabled, players without the permission will also not be protected from damage from other players (if the attacking
player has pvp enabled or no permission like them).

Players with the permission `pvptoggle.others` will be able to change the PvP status of other players with the command `/pvp <on|off> <player>`. You should only give this command
to staff members.

## Data Storage
By default, player's PvP status will be stored in an SQLite database by their unique ID. The value will be updated every time the status is changed and when the server is stopped.
When the server starts back up the player's uuid and PvP status are put back into memory for easy access.

The plugin also allows MySQL to be used for data storage instead of SQLite. This is for servers running BungeeCord that want player's PvP status to sync between servers.
If you want to use MySQL, it must be enabled in the config file.

## Hooking into PvPToggle
If you want to hook into PvPToggle from a different plugin, all of the main features can be easily accessed. Simply download the latest jar file and add it is a dependency 
in your plugin. Don't forget to add PvPToggle as a soft dependency, or hard dependency, in your plugin.yml. 

You can access the PvP status change event the same way you access standard Bukkit events. All other methods, such as the one to check if a player has PvP on, can be accessed 
simply by doing `PvPToggle.getCore().hasPvPOn(player)` Note: This returns a boolean. 

**Example:**
```
if (PvPToggle.getCore().hasPvPOn(player)) {
    // Do something
    PvPToggle.getCore().setStatus(player.getUniqueId(), true); // This would set their PvP status to true
}
```
