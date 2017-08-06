package main.java.CharacterBox.ClassBox;

/*
 * Starting funds for a character
 */
public class Funds {
    // number of d4 to getStringForRoll
    private int quantity;
    // true: multiply by 10
    private boolean multiply;

    public Funds(int quantity, boolean multiply) {
        this.quantity = quantity;
        this.multiply = multiply;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isMultiply() {
        return multiply;
    }
}
