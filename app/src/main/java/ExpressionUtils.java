import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiFunction;

public class ExpressionUtils {
  public static String cleaner(String form) {
    return form.substring(1).replace(" ", "");
  }

  public static Optional<Token> tokenizer(String text) {
    boolean openNum = false;
    String tempNumStr = "";

    int pCounter = 0;
    int pStartI = 0;

    ArrayList<Token> tokens = new ArrayList<Token>();
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);

      if (ch == '(') {
        if (pCounter == 0)
          pStartI = i;
        pCounter++;
        continue;
      }

      if (pCounter > 0) {
        if (ch != ')')
          continue;

        pCounter--;

        if (pCounter == 0) {
          Optional<Token> optionalToken = tokenizer(text.substring(pStartI + 1, i));
          if (optionalToken.isPresent()) {
            Token token = optionalToken.get();
            tokens.add(token);
          }
          else {
            return Optional.empty();
          }
        }

        continue;
      }

      if (Character.isDigit(ch) || ch == '.') {
        openNum = true;
        tempNumStr += ch;
        continue;
      } else if (openNum) {
        openNum = false;
        try {
          tokens.add(new TokenNumber(Double.parseDouble(tempNumStr)));
        } catch (Exception e) {
          return Optional.empty();
        }
        tempNumStr = "";
      }

      switch (ch) {
        case '+':
          tokens.add(new TokenAddSub(AddSub.ADD));
          continue;
        case '-':
          tokens.add(new TokenAddSub(AddSub.SUB));
          continue;
        case '*':
          tokens.add(new TokenMulDiv(MulDiv.MUL));
          continue;
        case '/':
          tokens.add(new TokenMulDiv(MulDiv.DIV));
          continue;
      }

      return Optional.empty();
    }

    if (openNum) {
      try {
        tokens.add(new TokenNumber(Double.parseDouble(tempNumStr)));
      } catch (Exception e) {
        return Optional.empty();
      }
    }

    if (tokens.size() == 1)
      return Optional.of((TokenNumber) tokens.get(0));

    return Optional.of(new TokenArray(tokens));
  }

  public static boolean isValid(Token token) {
    if (token instanceof TokenMulDiv || token instanceof TokenAddSub)
      return false;

    if (token instanceof TokenNumber)
      return true;

    if (token instanceof TokenArray tokenArray) {
      ArrayList<Token> tokens = tokenArray.value;

      for (int i = 0; i < tokens.size(); i++) {
        boolean isValid = switch (tokens.get(i)) {
          case TokenArray tokenArrayInner -> isValid(tokenArrayInner);
          case TokenMulDiv tokenMulDiv ->
            i > 0 && i < tokens.size() - 1
                && isTokenNumeral(tokens.get(i - 1))
                && isTokenNumeral(tokens.get(i + 1));
          case TokenAddSub tokenAddSub ->
            i < tokens.size() - 1
                && isTokenNumeral(tokens.get(i + 1));
          case TokenNumber tokenNumber -> true;
        };

        if (!isValid)
          return false;
      }

    }

    return true;
  }

  public static TokenNumber collapser(ArrayList<Token> tokens) {

    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i) instanceof TokenArray token) {
        tokens.set(i, collapser(token.value));
      }
    }

    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i) instanceof TokenMulDiv token) {
        double num0 = ((TokenNumber) tokens.get(i - 1)).value;
        double num1 = ((TokenNumber) tokens.get(i + 1)).value;

        BiFunction<Double, Double, Double> op = switch (token.value) {
          case MUL -> (x, y) -> x * y;
          case DIV -> (x, y) -> x / y;
        };

        tokens.set(i, new TokenNumber(op.apply(num0, num1)));
        tokens.remove(i + 1);
        tokens.remove(i - 1);
      }
    }

    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i) instanceof TokenAddSub token) {
        double num0 = i - 1 >= 0 ? ((TokenNumber) tokens.get(i - 1)).value : 0;
        double num1 = ((TokenNumber) tokens.get(i + 1)).value;

        BiFunction<Double, Double, Double> op = switch (token.value) {
          case ADD -> (x, y) -> x + y;
          case SUB -> (x, y) -> x - y;
        };

        tokens.set(i, new TokenNumber(op.apply(num0, num1)));
        tokens.remove(i + 1);
        if (i - 1 >= 0)
          tokens.remove(i - 1);
      }
    }

    return (TokenNumber) tokens.get(0);
  }

  static boolean isTokenNumeral(Token token) {
    return token instanceof TokenNumber || token instanceof TokenArray;
  }
}
