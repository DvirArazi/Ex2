
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

    /**
     * Initializes the Cell
     * @param s the value of the line
     */
    public SCell(String s) {
        setData(s);
    }

    /**
     * Initializes the Cell
     * @param s the value of the line
     * @param sheet reference for the containing Sheet
     * @param coord the coordinate of the Cell in the Sheet
     */
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

    /**
     * Returns whether the text contains a number
     * @param text 
     * @return
    */
    public boolean isNumber(String text) {
        return sheet.compute(text, List.of(coord)) instanceof BoxedNum;
    }

    /**
     * Returns whether the text parameter contains generic text 
     * @param text
     * @return
     */
    public boolean isText(String text) {
        return sheet.compute(text, List.of(coord)) instanceof BoxedText;
    }

    /**
     * Returns whether the text contains a valid math expression
     * @param text
     * @return
     */
    public boolean isForm(String text) {
        return sheet.compute(text, List.of(coord)) instanceof BoxedExprNum;
    }

    /**
     * Returns the computed value of the form
     * @param form
     * @return
     */
    public double computeForm(String form) {
        if (sheet.compute(form, List.of(coord)) instanceof BoxedExprNum boxedExprNum)
            return boxedExprNum.value;

        return -1;
    }
}
