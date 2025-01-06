
// Add your documentation below:

import java.util.List;

import Boxed.BoxedExprNum;
import Boxed.BoxedNum;
import Boxed.BoxedText;

public class SCell implements Cell {
    private String line;
    private int type;
    private Ex2Sheet sheet;
    private Coord coord;
    public int order;

    public SCell(String s) {
        setData(s);
    }

    public SCell(String s, Ex2Sheet sheet, Coord coord) {
        setData(s);
        this.sheet = sheet;
        this.coord = coord;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        line = s;
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        order = t;
    }

    public boolean isNumber(String text) {
        return sheet.compute(text, List.of(coord)) instanceof BoxedNum;
    }

    public boolean isText(String text) {
        return sheet.compute(text, List.of(coord)) instanceof BoxedText;
    }

    public boolean isForm(String text) {
        return sheet.compute(text, List.of(coord)) instanceof BoxedExprNum;
    }

    public double computeForm(String form) {
        if (sheet.compute(form, List.of(coord)) instanceof BoxedExprNum boxedExprNum)
            return boxedExprNum.value;

        return -1;
    }
}
