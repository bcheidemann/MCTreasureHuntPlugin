package uk.co.catlord.spigot.MCTreasureHuntPlugin.errors;

import java.util.ArrayList;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.interfaces.Pretty;

public class ErrorReport<TContext extends Pretty> implements Pretty {
  private TContext context;
  private String message;
  private ArrayList<Pretty> details;

  public ErrorReport(TContext context, String message) {
    this.context = context;
    this.message = message;
    this.details = new ArrayList<>();
  }

  public ErrorReport(TContext context, String message, ArrayList<Pretty> details) {
    this.context = context;
    this.message = message;
    this.details = details;
  }

  @Override
  public String pretty() {
    StringBuilder message = new StringBuilder();
    message.append(this.message + " " + context.pretty());

    if (details.size() == 0) {
      return message.toString();
    }

    message.append(":\n\n");

    for (Pretty error : details) {
      message.append(error.pretty().indent(2));
      message.append("\n");
    }

    return message.toString();
  }

  public Error intoError() {
    return new Error(pretty());
  }
}
