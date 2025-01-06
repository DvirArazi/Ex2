import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class Ex2Test {
  @Test
  public void Ex2SheetValueTest() {
    Ex2Sheet sheet = new Ex2Sheet();
    sheet.set(0, 0, "=1-1+1");

    assertEquals(sheet.value(0, 0), "1.0");
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
}
