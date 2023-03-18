package uk.co.catlord.spigot.MCTreasureHuntPlugin.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileLoader {
  public static String load(String fileName) throws IOException {
    FileReader fileReader;

    try {
      fileReader = new FileReader(fileName);
    } catch (FileNotFoundException e) {
      return null;
    }

    BufferedReader bufferedReader = new BufferedReader(fileReader);

    StringBuilder stringBuilder = new StringBuilder();

    String line = null;
    String ls = System.getProperty("line.separator");

    while ((line = bufferedReader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(ls);
    }

    // delete the last new line separator and close the reader
    if (stringBuilder.length() > 0) {
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }

    bufferedReader.close();

    return stringBuilder.toString();
  }
}
