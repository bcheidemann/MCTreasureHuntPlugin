package uk.co.catlord.spigot.MCTreasureHuntPlugin.errors;

import java.util.ArrayList;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.interfaces.Pretty;

public class ErrorPathContext implements Pretty {
  private ArrayList<String> pathSegments = new ArrayList<>();

  public ErrorPathContext(String... path) {
    for (String segment : path) {
      pathSegments.add(segment);
    }
  }

  public ErrorPathContext(ErrorPathContext parent, String... path) {
    pathSegments.addAll(parent.pathSegments);

    for (String segment : path) {
      pathSegments.add(segment);
    }
  }

  public String pretty() {
    return "at '" + String.join(".", pathSegments) + "'";
  }

  public ErrorPathContext extend(String... path) {
    return new ErrorPathContext(this, path);
  }
}
