package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.io.IOException;
import org.json.JSONObject;

public class JsonFileLoader {
  public static JSONObject loadJSONObject(String fileName) throws IOException {
    String content = FileLoader.load(fileName);

    if (content == null) {
      throw new IOException("Missing config file: " + fileName);
    }

    return new JSONObject(content);
  }
}
