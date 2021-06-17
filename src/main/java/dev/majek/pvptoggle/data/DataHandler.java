package dev.majek.pvptoggle.data;

import dev.majek.pvptoggle.PvPToggle;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class DataHandler {

  private final JSONConfig config;

  public DataHandler() {
    config = new JSONConfig(PvPToggle.getCore().getDataFolder(), "pvp");
    try {
      config.createConfig();
    } catch (FileNotFoundException e) {
      PvPToggle.getCore().getLogger().severe("Unable to create pvp.json storage file!");
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public void updatePvPStorage(UUID uuid, boolean toggle) {
    if (PvPToggle.usingMySQL) {
      PvPToggle.getMySQL().updateStatus(uuid);
    } else {
      JSONObject pvpJson = new JSONObject();
      pvpJson.put(uuid.toString(), toggle);

      try {
        config.putInJSONObject(pvpJson);
      } catch (IOException | ParseException e) {
        PvPToggle.getCore().getLogger().severe("Unable to save player (uuid: \"" + uuid + "\") to pvp.json");
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings("unused")
  public void loadPvPStorage() {
    if (PvPToggle.usingMySQL)
      PvPToggle.getMySQL().getAllStatuses();
    else
      loadFromJson();
  }

  public void loadFromJson() {
    JSONObject fileContents;
    try {
      fileContents = config.toJSONObject();
    } catch (IOException | ParseException e) {
      PvPToggle.getCore().getLogger().severe("Critical error loading saved parties from parties.json");
      e.printStackTrace();
      return;
    }
    for (Object key : fileContents.keySet()) {
      UUID playerID = UUID.fromString(key.toString());
      boolean status = Boolean.getBoolean(fileContents.get(key).toString());
      PvPToggle.getCore().setStatus(playerID, status);
    }
  }
}
