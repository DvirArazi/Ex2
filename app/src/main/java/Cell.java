import java.util.Optional;

public class Cell {
  public String text;

  public Cell(String text) {
    this.text = text;
  }

  public boolean isNumberFix() {
    return isNumber(text);
  }

  public boolean isTextFix() {
    return isText(text);
  }

  public boolean isFormFix() {
    return isForm(text);
  }

  public double computeFormFix() {
    return computeForm(text);
  }

  boolean isNumber(String text) {
    try {
      Double.parseDouble(text);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  boolean isText(String text) {
    if (text.startsWith("="))
      return false;

    return !isNumber(text);
  }

  boolean isForm(String text) {
    if (!text.startsWith("="))
      return false;

    text = ExpressionUtils.cleaner(text);
    Optional<Token> optionalTokenArray = ExpressionUtils.tokenizer(text);
    if (optionalTokenArray.isEmpty())
      return false;
    Token tokenArray = (Token) optionalTokenArray.get();
    return ExpressionUtils.isValid(tokenArray);
  }

  double computeForm(String form) {
    String expression = ExpressionUtils.cleaner(form);
    Token token = ExpressionUtils.tokenizer(expression).get();

    if (token instanceof TokenNumber tokenNumber) {
      return tokenNumber.value;
    }

    double result = ExpressionUtils.collapser(((TokenArray)token).value).value;

    return result;
  }
}
