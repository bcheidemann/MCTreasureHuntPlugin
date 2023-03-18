package uk.co.catlord.spigot.MCTreasureHuntPlugin.errors;

public class Result<T, E> {
  private T value;
  private E error;

  public Result(T value, E error) {
    this.value = value;
    this.error = error;
  }

  public static <T, E> Result<T, E> ok(T value) {
    return new Result<T, E>(value, null);
  }

  public static <T, E> Result<T, E> error(E error) {
    return new Result<T, E>(null, error);
  }

  public T getValue() {
    return value;
  }

  public E getError() {
    return error;
  }

  public boolean isOk() {
    return error == null;
  }

  public boolean isError() {
    return error != null;
  }
}
