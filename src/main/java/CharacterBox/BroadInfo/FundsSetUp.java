package CharacterBox.BroadInfo;

import CoreBox.Die;



/*
 * Calculates starting funds for a character
 */
class FundsSetUp {
    // number of d4 to roll
    private int quantity;
    // true: multiply by 10
    private boolean multiply;


    FundsSetUp(int quantity, boolean multiply) {
        this.quantity = quantity;
        this.multiply = multiply;
    }


    int rollFunds() {
        int roll = new Die(quantity, 4, 0).roll().getTotal();

        if (multiply) {
            roll *= 10;
        }

        return roll;
    }
}
