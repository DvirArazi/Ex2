
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Boxed.BoxedCyclErr;
import Boxed.BoxedNum;
import Boxed.BoxedFormErr;
import Boxed.BoxedExprNum;
import Boxed.BoxedText;
import Boxed.Collapsable;
import Boxed.Computable;

public class Ex2Sheet implements Sheet {
  private Cell[][] table;

  public Ex2Sheet(int x, int y) {
    table = new SCell[x][y];
    for (int i = 0; i < x; i = i + 1) {
      for (int j = 0; j < y; j = j + 1) {
        table[i][j] = new SCell("");
      }
    }
    eval();
  }

  public Ex2Sheet() {
    this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
  }

  @Override
  public String value(int x, int y) {
    return eval(x, y);
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
    Cell c = new SCell(s);
    table[x][y] = c;
  }

  @Override
  public void eval() {
    // Add your code here

    //////////////////////
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

      }
    }

    return ans;
  }

  int depthSingle(List<Coord> coords) {
    Coord last = coords.getLast();
    Cell cell = table[last.x][last.y];
    String line = cell.getData();

    boolean openCoord = false;

    ArrayList<String> parts = new ArrayList<>();

    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (!openCoord) {
        if (Character.isAlphabetic(ch)) {
          openCoord = true;
          parts.add(ch + "");
        }
      } else {
        if (Character.isDigit(ch))
          parts.set(parts.size() - 1, parts.getLast() + ch);
        else
          openCoord = false;
      }
    }

    ArrayList<Coord> newCoords = new ArrayList<Coord>();
    for (int i = 0; i < parts.size(); i++) {
      Coord newCoord = parseCoord(parts.get(i)).get();

      if (containsCoord(coords, newCoord))
        return -1;

      newCoords.add(newCoord);
    }

    // for (int i = 0;)

    return 0;
  }

  @Override
  public void load(String fileName) throws IOException {
    // Add your code here

    /////////////////////
  }

  @Override
  public void save(String fileName) throws IOException {
    // Add your code here

    /////////////////////
  }

  @Override
  public String eval(int x, int y) {
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

  Computable compute(String line, List<Coord> coords) {
    if (line.startsWith("=")) {
      Coord coord = coords.getLast();
      String expr = line.substring(1).replaceAll(" ", "");
      Collapsable result = collapse(expr, List.of(coord));
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

  Collapsable collapse(String expr, List<Coord> coords) {
    while (expr.startsWith("(") && expr.endsWith(")"))
      expr = expr.substring(1, expr.length() - 1);

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

  Optional<Integer> getOpIndex(String expr) {
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

  boolean isOp(char ch) {
    return ch == '+' || ch == '-' || ch == '*' || ch == '/';
  }

  boolean isOpBetter(int db, int ob, int ib, int dc, int oc, int ic) {
    return db > dc || db == dc && (ob > oc || ob == oc && ib > ic);
  }

  Optional<Integer> opRankOf(char op) {
    return switch (op) {
      case '+' -> Optional.of(0);
      case '-' -> Optional.of(0);
      case '*' -> Optional.of(1);
      case '/' -> Optional.of(1);
      default -> Optional.empty();
    };
  }

  Optional<Coord> parseCoord(String s) {
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

  Optional<Double> calculateByUnOp(double n, char op) {
    return switch (op) {
      case '+' -> Optional.of(n);
      case '-' -> Optional.of(-n);
      default -> Optional.empty();
    };
  }

  Optional<Double> calculateByBiOp(double n0, double n1, char op) {
    return switch (op) {
      case '+' -> Optional.of(n0 + n1);
      case '-' -> Optional.of(n0 - n1);
      case '*' -> Optional.of(n0 * n1);
      case '/' -> Optional.of(n0 / n1);
      default -> Optional.empty();
    };
  }

  Optional<Double> parseDouble(String s) {
    try {
      return Optional.of(Double.parseDouble(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  Optional<Integer> parseInt(String s) {
    try {
      return Optional.of(Integer.parseInt(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  boolean containsCoord(List<Coord> coords, Coord coord) {
    for (int i = 0; i < coords.size(); i++) {
      Coord coordCrnt = coords.get(i);
      if (coord.x == coordCrnt.x && coord.y == coordCrnt.y)
        return true;
    }

    return false;
  }
}
