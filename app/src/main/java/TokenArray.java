import java.util.ArrayList;

public final class TokenArray implements Token {
  public ArrayList<Token> value;

  public TokenArray(ArrayList<Token> value) {
    this.value = value;
  }
}
