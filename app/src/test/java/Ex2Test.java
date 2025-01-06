import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class Ex2Test {
  @Test
  public void Ex2SheetValueTest() {
    Ex2Sheet sheet = new Ex2Sheet();
    sheet.set(0, 0, "=1-1+1");
    sheet.set(1, 0, "=(34+56*27)-2");
    sheet.set(2, 0, "=(34+B0*27)-2");
    sheet.set(3, 0, "=D0+5");
    sheet.set(4, 0, "=F0+5");
    sheet.set(5, 0, "=E0+5");
    sheet.set(6, 0, "=5-*7");
    sheet.set(7, 0, "=(4+5)*(4/2)");

    assertEquals(sheet.value(0, 0), "1.0");
    assertEquals(sheet.value(1, 0), "1544.0");
    assertEquals(sheet.value(2, 0), "41720.0");
    assertEquals(sheet.value(3, 0), "ERR_CYCLE!");
    assertEquals(sheet.value(4, 0), "ERR_CYCLE!");
    assertEquals(sheet.value(6, 0), "ERR_FORM!");
    assertEquals(sheet.value(7, 0), "18.0");
  }

  @Test
  public void Ex2SheetGetTypeTest() {
    Ex2Sheet sheet = new Ex2Sheet();
    sheet.set(0, 0, "=(34+56*27)-2");
    sheet.set(1, 0, "123");
    sheet.set(2, 0, "Stam Text");
    sheet.set(3, 0, "=D1");
    sheet.set(3, 1, "=D0");
    sheet.set(4, 0, "=543**34");

    for (int x = 0; x < 5; x++) {
      sheet.value(x, 0);
    }

    assertEquals(sheet.get(0, 0).getType(), Ex2Utils.FORM);
    assertEquals(sheet.get(1, 0).getType(), Ex2Utils.NUMBER);
    assertEquals(sheet.get(2, 0).getType(), Ex2Utils.TEXT);
    assertEquals(sheet.get(3, 0).getType(), Ex2Utils.ERR_CYCLE_FORM);
    assertEquals(sheet.get(4, 0).getType(), Ex2Utils.ERR_FORM_FORMAT);
  }

  @Test
  public void Ex2SheetDepthOneTest() {
    Ex2Sheet sheet = new Ex2Sheet();
    sheet.set(0, 0, "test");
    sheet.set(1, 0, "123");
    sheet.set(2, 0, "=123");
    sheet.set(3, 0, "=B0+7");
    sheet.set(4, 0, "=(D0*5)");
    sheet.set(5, 0, "=F0");
    sheet.set(6, 0, "=E0");

    assertEquals(sheet.depthSingle(List.of(new Coord(0, 0))), 0);
    assertEquals(sheet.depthSingle(List.of(new Coord(1, 0))), 0);
    assertEquals(sheet.depthSingle(List.of(new Coord(2, 0))), 1);
    assertEquals(sheet.depthSingle(List.of(new Coord(3, 0))), 2);
    assertEquals(sheet.depthSingle(List.of(new Coord(4, 0))), 3);
    assertEquals(sheet.depthSingle(List.of(new Coord(5, 0))), -1);
  }

  @Test
  public void SCellIsForm() {
    Ex2Sheet sheet = new Ex2Sheet();

    String[] forms = new String[] { "=*5+3", "=5+)5-(2=1", "=3+(-(-(3)))", "=2+-1" };
    boolean[] answers = new boolean[] { false, false, true, false };

    for (int i = 0; i < forms.length; i++) {
      sheet.set(0, 0, forms[i]);
      SCell cell = (SCell) sheet.get(0, 0);
      assertEquals(cell.isForm(cell.getData()), answers[i]);
    }
  }
}
