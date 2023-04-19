package uk.co.catlord.spigot.MCTreasureHuntPlugin.errors;

import java.util.ArrayList;
import uk.co.catlord.spigot.MCTreasureHuntPlugin.interfaces.Pretty;

public class ErrorReportBuilder<TContext extends Pretty> {
  private TContext context;
  private String message;
  private ArrayList<Pretty> details = new ArrayList<>();

  public ErrorReportBuilder(TContext context, String message) {
    this.context = context;
    this.message = message;
  }

  public ErrorReportBuilder<TContext> addDetail(Pretty detail) {
    details.add(detail);
    return this;
  }

  public ErrorReport<TContext> build() {
    return new ErrorReport<>(context, message, details);
  }

  public boolean hasErrors() {
    return !details.isEmpty();
  }
}
