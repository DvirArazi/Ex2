import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

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
    sheet.set(8, 0, "=()");
    sheet.set(0, 1, "=59-");
    sheet.set(0, 2, "=-A0");
    sheet.set(0, 3, "=A0-");
    sheet.set(0, 4, "=bla");

    assertEquals(sheet.value(0, 0), "1.0");
    assertEquals(sheet.value(1, 0), "1544.0");
    assertEquals(sheet.value(2, 0), "41720.0");
    assertEquals(sheet.value(3, 0), "ERR_CYCLE!");
    assertEquals(sheet.value(4, 0), "ERR_CYCLE!");
    assertEquals(sheet.value(6, 0), "ERR_FORM!");
    assertEquals(sheet.value(7, 0), "18.0");
    assertEquals(sheet.value(8, 0), "ERR_FORM!");
    assertEquals(sheet.value(0, 1), "ERR_FORM!");
    assertEquals(sheet.value(0, 2), "-1.0");
    assertEquals(sheet.value(0, 3), "ERR_FORM!");
    assertEquals(sheet.value(0, 4), "ERR_FORM!");
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

    int[][] depth = sheet.depth();

    assertEquals(depth[0][0], 0);
    assertEquals(depth[1][0], 0);
    assertEquals(depth[2][0], 1);
    assertEquals(depth[3][0], 2);
    assertEquals(depth[4][0], 3);
    assertEquals(depth[5][0], -1);
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

  @Test
  public void Ex2Index2DToStringTest() {
    Ex2Index2D index2d = new Ex2Index2D(5, 13);

    assertEquals("F13", index2d.toString());
  }

  @Test
  public void Ex2SheetGetInners() {
    Coord c0 = new Coord(5, 7);
    Coord c1 = new Coord(5, 7);

    assertEquals(c0, c1);

    assertEquals(
        Set.of(new Coord(5, 13), new Coord(3, 3)),
        Ex2Sheet.getInners("F13+56*D3/(1+D3)"));

    assertEquals(
        Set.of(new Coord(1, 0)),
        Ex2Sheet.getInners("B0+7"));

    assertEquals(
        Set.of(new Coord(3, 0)),
        Ex2Sheet.getInners("(D0*5)"));
  }
}
