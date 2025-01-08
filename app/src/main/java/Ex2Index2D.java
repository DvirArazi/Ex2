public class Ex2Index2D implements Index2D {
  private int x;
  private int y;

  public Ex2Index2D(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return (char)(x + 'A') + String.valueOf(y);
  }

  @Override
  public boolean isValid() {
    return x >= 0 && y >= 0 && x < 26 && y < 100;
  }

  @Override
  public int getX() {
    return x;
  }

  @Override
  public int getY() {
    return y;
  }
}
