package uk.co.catlord.spigot.MCTreasureHuntPlugin.errors;

import uk.co.catlord.spigot.MCTreasureHuntPlugin.interfaces.Pretty;

public class ErrorDetail implements Pretty {
  private String message;

  public ErrorDetail(String message) {
    this.message = message;
  }

  @Override
  public String pretty() {
    return message;
  }

  public Error intoError() {
    return new Error(pretty());
  }
}
