public class Spreadsheet {
  Cell[][] cells;

  public Spreadsheet(int x, int y) {
    cells = new Cell[y][x];
  }

  public Cell get(int x, int y) {
    return cells[y][x];
  }

  public void set(int x, int y, Cell c) {
    cells[y][x] = c;
  }

  public int width() {
    return cells[0].length;
  }

  public int height() {
    return cells.length;
  }

  public int xCell(String c) {
    return c.charAt(0) - 'A';
  }

  public int yCell(String c) {
    return Integer.parseInt(c.substring(1));
  }

  public String eval(int x, int y) {
    return String.valueOf(cells[y][x].computeFormFix());
  }

  public String[][] evalAll() {
    String[][] result = new String[height()][width()];

    for (int y = 0; y < height(); y++) {
      for (int x = 0; x < width(); x++) {
        result[y][x] = eval(x, y);
      }
    }

    return result;
  }

  int depthOne(int x, int y) {
    Cell cell = cells[y][x];
    String text = cell.text;
    if (cell.isNumber(text) || cell.isText(text)) return 0;

    int pCount = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '(') pCount++;
    }

    if (cell.isForm(text)) return 1 + pCount;

    return -1;
  }

  public int[][] depth() {
    int[][] result = new int[height()][width()];

    for (int y = 0; y < height(); y++) {
      for (int x = 0; x < width(); x++) {
        result[y][x] = depthOne(x, y);
      }
    }

    return result;
  }
}
