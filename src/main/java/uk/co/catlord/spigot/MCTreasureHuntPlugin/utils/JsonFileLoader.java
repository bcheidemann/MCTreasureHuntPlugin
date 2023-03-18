package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.errors.Result;

public class JsonFileLoader {
  public static JSONObject loadJSONObject(String fileName) throws IOException {
    String content = FileLoader.load(fileName);

    if (content == null) {
      throw new IOException("Missing config file: " + fileName);
    }

    return new JSONObject(content);
  }

  public static Result<Boolean, Exception> saveJSONObject(String fileName, JSONObject data) {
    try {
      File file = new File(fileName);
      FileWriter writer = new FileWriter(file);
      writer.write(data.toString(2));
      writer.close();
      return Result.ok(true);
    } catch (Exception e) {
      return Result.error(e);
    }
  }
}
