

// Add your documentation below:

public class CellEntry implements Index2D {

    String repr;

    @Override
    public boolean isValid() {
        return Ex2Sheet.parseCoord(repr).isPresent();
    }

    @Override
    public int getX() {
        return Ex2Sheet.parseCoord(repr).get().x;
    }

    @Override
    public int getY() {
        return Ex2Sheet.parseCoord(repr).get().y;
    }
}
