package CharacterBox.ClassBox;

import Foo.Roll;



/*
 * Starting funds for a character
 */
public class Funds {
    // number of d4 to rollDieFromChatEvent
    private int quantity;
    // true: multiply by 10
    private boolean multiply;

    public Funds(int quantity, boolean multiply) {
        this.quantity = quantity;
        this.multiply = multiply;
    }

    public int rollFunds() {
        int roll = new Roll(quantity, 4, 0).roll().getResult();

        if (multiply) {
            roll *= 10;
        }

        return roll;
    }
}
