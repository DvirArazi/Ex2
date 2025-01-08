import java.util.Objects;

public class Coord {
  public int x;
  public int y;

  public Coord(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    Coord oCoord = (Coord) obj;
    return x == oCoord.x && y == oCoord.y;
  }
}
