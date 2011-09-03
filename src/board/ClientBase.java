package board;

/**
 * Handles the {@link #setName} semantic.
 */
public abstract class ClientBase implements Client {
  private String name;

  public void setName (String name) {
    this.name = name;
  }

  public String getName () {
    return name;
  }
}
