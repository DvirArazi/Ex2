import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import Boxed.BoxedCyclErr;
import Boxed.BoxedNum;
import Boxed.BoxedFormErr;
import Boxed.BoxedExprNum;
import Boxed.BoxedText;
import Boxed.Collapsable;
import Boxed.Computable;

public class Ex2Sheet implements Sheet {
  private Cell[][] table;

  /**
   * Initializes the Sheet
   * 
   * @param x
   * @param y
   */
  public Ex2Sheet(int x, int y) {
    table = new SCell[x][y];
    for (int i = 0; i < x; i = i + 1) {
      for (int j = 0; j < y; j = j + 1) {
        table[i][j] = new SCell("", this, new Coord(x, y));
      }
    }
    eval();
  }

  /**
   * Initializes the sheet with the constants WIDTH and HEIGHT
   */
  public Ex2Sheet() {
    this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
  }

  @Override
  public String value(int x, int y) {
    Cell cell = table[x][y];
    String line = cell.getData();

    Computable computable = compute(line, List.of(new Coord(x, y)));

    cell.setType(switch (computable) {
      case BoxedText boxedText -> Ex2Utils.TEXT;
      case BoxedExprNum boxedExprNum -> Ex2Utils.FORM;
      case BoxedNum boxedNum -> Ex2Utils.NUMBER;
      case BoxedFormErr boxedFormErr -> Ex2Utils.ERR_FORM_FORMAT;
      case BoxedCyclErr boxedCyclErr -> Ex2Utils.ERR_CYCLE_FORM;
    });

    return switch (computable) {
      case BoxedText boxedText -> boxedText.value;
      case BoxedExprNum boxedExprNum -> String.valueOf(boxedExprNum.value);
      case BoxedNum boxedNum -> String.valueOf(boxedNum.value);
      case BoxedFormErr boxedFormErr -> Ex2Utils.ERR_FORM;
      case BoxedCyclErr boxedCyclErr -> Ex2Utils.ERR_CYCLE;
    };
  }

  @Override
  public Cell get(int x, int y) {
    return table[x][y];
  }

  @Override
  public Cell get(String cords) {
    Coord coord = parseCoord(cords).get();

    return table[coord.x][coord.y];
  }

  @Override
  public int width() {
    return table.length;
  }

  @Override
  public int height() {
    return table[0].length;
  }

  @Override
  public void set(int x, int y, String s) {
    Cell c = new SCell(s, this, new Coord(x, y));
    table[x][y] = c;
  }

  @Override
  public void eval() {
    for (int x = 0; x < width(); x++) {
      for (int y = 0; y < height(); y++) {
        eval(x, y);
      }
    }
  }

  @Override
  public boolean isIn(int x, int y) {
    return x >= 0 && x < width() && y >= 0 && y < height();
  }

  @Override
  public int[][] depth() {
    int[][] ans = new int[width()][height()];
    for (int x = 0; x < width(); x++) {
      for (int y = 0; y < height(); y++) {
        ans[x][y] = Integer.MAX_VALUE;
      }
    }

    boolean done = false;
    while (!done) {
      done = true;
      for (int x = 0; x < width(); x++) {
        for (int y = 0; y < height(); y++) {
          SCell cell = (SCell) table[x][y];
          String line = cell.getData();
          Computable computed = compute(line, List.of());

          if (computed instanceof BoxedCyclErr)
            ans[x][y] = -1;
          else if (computed instanceof BoxedNum ||
              computed instanceof BoxedText)
            ans[x][y] = 0;
          else if (computed instanceof BoxedExprNum ||
              computed instanceof BoxedFormErr) {
            int largestDepth = Integer.MIN_VALUE;

            Set<Coord> inners = getInners(line.substring(1));

            if (inners.isEmpty()) {
              ans[x][y] = 1;
              continue;
            }

            for (Coord coord : inners)
              largestDepth = Math.max(largestDepth, ans[coord.x][coord.y]);
            if (largestDepth == Integer.MAX_VALUE) {
              done = false;
              continue;
            }

            if (largestDepth == 0)
              largestDepth = 1;
            ans[x][y] = largestDepth + 1;
          }
        }
      }
    }

    return ans;

  }

  public static Set<Coord> getInners(String line) {
    line = removePs(line);

    Optional<Coord> optionalCoord = parseCoord(line);
    if (optionalCoord.isPresent()) {
      Coord coord = optionalCoord.get();
      return Set.of(coord);
    }

    Optional<Integer> optionalOpI = getOpIndex(line);
    if (optionalOpI.isEmpty())
      return Set.of();

    int opI = optionalOpI.get();

    Set<Coord> set0 = getInners(line.substring(0, opI));
    Set<Coord> set1 = getInners(line.substring(opI + 1));

    return Sets.union(set0, set1);
  }

  @Override
  public void load(String fileName) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(fileName));

    for (int x = 0; x < width(); x++) {
      for (int y = 0; y < height(); y++) {
        table[x][y].setData("");
      }
    }

    for (int i = 1; i < lines.size(); i++) {
      String[] split = lines.get(i).split(",");
      int x = Integer.parseInt(split[0]);
      int y = Integer.parseInt(split[1]);

      table[x][y].setData(split[2]);
    }
  }

  @Override
  public void save(String fileName) throws IOException {
    String content = "First line: just a header line - should not be parsed.\n";

    for (int y = 0; y < height(); y++) {
      for (int x = 0; x < width(); x++) {
        SCell cell = (SCell) table[x][y];
        String line = cell.getData();
        if (line != "") {
          content += x + "," + y + "," + line + "\n";
        }
      }
    }

    FileWriter writer = new FileWriter(fileName);
    writer.write(content);
    writer.close();
  }

  @Override
  public String eval(int x, int y) {
    return value(x, y);
  }

  /**
   * Computes the value of a given Cell
   * 
   * @param line
   * @param coords
   * @return
   */
  Computable compute(String line, List<Coord> coords) {
    if (line.startsWith("=")) {
      String expr = line.substring(1).replaceAll(" ", "");
      Collapsable result = collapse(expr, coords);
      return switch (result) {
        case BoxedExprNum boxedExprNum -> boxedExprNum;
        case BoxedFormErr boxedFormErr -> boxedFormErr;
        case BoxedCyclErr boxedCyclErr -> boxedCyclErr;
      };
    }

    Optional<Double> optionalNum = parseDouble(line);
    if (optionalNum.isPresent()) {
      double num = optionalNum.get();
      return new BoxedNum(num);
    }

    return new BoxedText(line);
  }

  /**
   * Collapses a math expression to a single numeric value
   * 
   * @param expr
   * @param coords
   * @return
   */
  Collapsable collapse(String expr, List<Coord> coords) {
    expr = removePs(expr);

    Optional<Integer> optionalOpIndex = getOpIndex(expr);

    if (optionalOpIndex.isEmpty()) {
      Optional<Double> optionalResult = parseDouble(expr);
      if (optionalResult.isPresent()) {
        double result = optionalResult.get();
        return new BoxedExprNum(result);
      }

      Optional<Coord> optionalCoord = parseCoord(expr);
      if (optionalCoord.isPresent()) {
        Coord coord = optionalCoord.get();
        if (containsCoord(coords, coord))
          return new BoxedCyclErr();

        if (!isIn(coord.x, coord.y))
          return new BoxedFormErr();

        Cell cell = table[coord.x][coord.y];
        String cellExpr = cell.getData();

        List<Coord> coordsCopy = new ArrayList<Coord>(coords);
        coordsCopy.add(coord);

        Computable result = compute(cellExpr, coordsCopy);

        return switch (result) {
          case BoxedNum boxedNum -> new BoxedExprNum(boxedNum.value);
          case BoxedText boxedText -> new BoxedFormErr();
          default -> (Collapsable) result;
        };
      }

      return new BoxedFormErr();
    }

    int opIndex = optionalOpIndex.get();

    String sub0 = expr.substring(0, opIndex);
    String sub1 = expr.substring(opIndex + 1);
    char op = expr.charAt(opIndex);

    if (sub0 != "") {
      Collapsable boxedNum0 = collapse(sub0, coords);
      Collapsable boxedNum1 = collapse(sub1, coords);

      if (boxedNum0 instanceof BoxedFormErr || boxedNum1 instanceof BoxedFormErr)
        return new BoxedFormErr();

      if (boxedNum0 instanceof BoxedCyclErr || boxedNum1 instanceof BoxedCyclErr)
        return new BoxedCyclErr();

      double num0 = ((BoxedExprNum) boxedNum0).value;
      double num1 = ((BoxedExprNum) boxedNum1).value;

      double result = calculateByBiOp(num0, num1, op).get();
      return new BoxedExprNum(result);
    }

    if (op == '+' || op == '-') {
      Collapsable boxedNum1 = collapse(sub1, coords);

      if (!(boxedNum1 instanceof BoxedExprNum))
        return boxedNum1;

      double num1 = ((BoxedExprNum) boxedNum1).value;

      double result = calculateByUnOp(num1, op).get();
      return new BoxedExprNum(result);
    }

    return new BoxedFormErr();
  }

  static String removePs(String expr) {
    pCheck: while (expr.startsWith("(") && expr.endsWith(")")) {
      int pCount = 0;
      for (int i = 1; i < expr.length() - 1; i++) {
        char ch = expr.charAt(i);

        if (ch == '(') {
          pCount++;
          continue;
        }

        if (ch == ')') {
          pCount--;

          if (pCount < 0)
            break pCheck;
        }
      }
      if (pCount != 0)
        break pCheck;

      expr = expr.substring(1, expr.length() - 1);
    }

    return expr;
  }

  /**
   * Returns the index of the last operator to be computed
   * 
   * @param expr
   * @return
   */
  static Optional<Integer> getOpIndex(String expr) {
    boolean found = false;

    int db = Integer.MAX_VALUE;
    int ob = Integer.MAX_VALUE;
    int ib = Integer.MAX_VALUE;

    int dc = 0;
    for (int ic = 0; ic < expr.length(); ic++) {
      char ch = expr.charAt(ic);

      if (ch == '(') {
        dc++;
        continue;
      }
      if (ch == ')') {
        dc--;
        continue;
      }

      Optional<Integer> optionalOc = opRankOf(ch);
      if (optionalOc.isEmpty())
        continue;

      int oc = optionalOc.get();

      if (isOp(ch) && isOpBetter(db, ob, ib, dc, oc, ic)) {
        found = true;
        db = dc;
        ob = oc;
        ib = ic;
      }
    }

    if (!found)
      return Optional.empty();

    return Optional.of(ib);
  }

  /**
   * Returns whether the char represents a math operation
   * 
   * @param ch
   * @return
   */
  static boolean isOp(char ch) {
    return ch == '+' || ch == '-' || ch == '*' || ch == '/';
  }

  /**
   * Checks whether `ib` is the latest operator to be computed or whether
   * it should be replaced with `ic`
   * 
   * @param db depth best
   * @param ob operator rank best
   * @param ib index best
   * @param dc depth current
   * @param oc operator rank current
   * @param ic index current
   * @return
   */
  static boolean isOpBetter(int db, int ob, int ib, int dc, int oc, int ic) {
    return db > dc || db == dc && (ob > oc || ob == oc && ib < ic);
  }

  /**
   * Returns the rank of the operator as determined by the order of operations
   * 
   * @param op
   * @return
   */
  static Optional<Integer> opRankOf(char op) {
    return switch (op) {
      case '+' -> Optional.of(0);
      case '-' -> Optional.of(0);
      case '*' -> Optional.of(1);
      case '/' -> Optional.of(1);
      default -> Optional.empty();
    };
  }

  /**
   * Converts a String represeting a coordinate to a Coord object
   * 
   * @param s
   * @return
   */
  public static Optional<Coord> parseCoord(String s) {
    if (s.length() < 1)
      return Optional.empty();

    char xChr = s.charAt(0);

    Optional<Integer> optionalX = Character.isUpperCase(xChr) ? Optional.of(xChr - 'A')
        : Character.isLowerCase(xChr) ? Optional.of(xChr - 'a')
            : Optional.empty();
    if (optionalX.isEmpty())
      return Optional.empty();

    int x = optionalX.get();

    Optional<Integer> optionalY = parseInt(s.substring(1));
    if (optionalY.isEmpty())
      return Optional.empty();

    int y = optionalY.get();

    return Optional.of(new Coord(x, y));
  }

  /**
   * Returns the result of the Unary operator `op` on `n`
   * 
   * @param n
   * @param op
   * @return
   */
  Optional<Double> calculateByUnOp(double n, char op) {
    return switch (op) {
      case '+' -> Optional.of(n);
      case '-' -> Optional.of(-n);
      default -> Optional.empty();
    };
  }

  /**
   * Returns the result of the Binary operator `op` on `n0` and `n1`
   * 
   * @param n0
   * @param n1
   * @param op
   * @return
   */
  Optional<Double> calculateByBiOp(double n0, double n1, char op) {
    return switch (op) {
      case '+' -> Optional.of(n0 + n1);
      case '-' -> Optional.of(n0 - n1);
      case '*' -> Optional.of(n0 * n1);
      case '/' -> Optional.of(n0 / n1);
      default -> Optional.empty();
    };
  }

  /**
   * Parses a double, returns empty if `s` does not represent a valid double
   * 
   * @param s
   * @return
   */
  public static Optional<Double> parseDouble(String s) {
    try {
      return Optional.of(Double.parseDouble(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Parses an int, returns empty if `s` does not represent a valid double
   * 
   * @param s
   * @return
   */
  public static Optional<Integer> parseInt(String s) {
    try {
      return Optional.of(Integer.parseInt(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Returns whether `coord` is contained in `coords`
   * 
   * @param coords
   * @param coord
   * @return
   */
  boolean containsCoord(List<Coord> coords, Coord coord) {
    for (int i = 0; i < coords.size(); i++) {
      Coord coordCrnt = coords.get(i);
      if (coord.x == coordCrnt.x && coord.y == coordCrnt.y)
        return true;
    }

    return false;
  }
}
