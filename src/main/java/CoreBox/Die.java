package CoreBox;

import ExceptionsBox.BadUserInputException;

import java.util.Random;



public class Die {
    public enum RollType {NORMAL, ADVANTAGE, DISADVANTAGE}
    private int quantity;
    private int dieSize;
    private int modifier;


    /*
     * TODO BestPractice Is it better to put defaults in here or in class constructor?
     */
    public Die(int dieSize) {
        quantity = 1;
        this.dieSize = dieSize;
        modifier = 0;
    }


    public Die(int quantity, int dieSize, int modifier) {
        this.quantity = quantity;
        this.dieSize = dieSize;
        this.modifier = modifier;
    }


    /*
     * Rolls a given dieSize once with no modifier so that a roll object doesn't need to be created by the caller
     */
    public static int quickRoll(int dieSize) {
        return new Die(dieSize).roll().total;
    }


    /*
     * Rolls the die as many times as required
     */
    public RollResult roll() {
        if (dieSize <= 1) {
            throw new BadUserInputException("Invalid die size");
        }

        final Random random = new Random();
        int[] rolls = new int[quantity];
        for (int j = 0; j < quantity; j++) {
            rolls[j] = random.nextInt(dieSize) + 1;
        }
        return new RollResult(rolls);
    }


    /*
     * Rolls the die assuming the rollType is 'NORMAL' and returns a string of the total
     */
    public String getStringForRoll() {
        return getStringForRoll(RollType.NORMAL);
    }


    /*
     * Rolls the die and returns the output as a string (inc. crits and fumbles)
     */
    public String getStringForRoll(RollType rollType) {
        if (dieSize == 1) {
            // TODO Improve add more effects
            // quantity is number of bad things to happen or always just one thing?
            return "has incurred Eywa's wrath and is struck by a bolt of lightning";
        }
        else {
            return roll(rollType).toString();
        }
    }


    /*
     * Rolls and applies advantage/disadvantage if necessary
     */
    public RollResult roll(RollType rollType) {
        final RollResult roll1 = roll();
        switch (rollType) {
            case ADVANTAGE:
                return max(roll1, roll());
            case DISADVANTAGE:
                return min(roll1, roll());
            case NORMAL:
            default:
                return roll1;
        }
    }


    private RollResult max(RollResult first, RollResult second) {
        if (first.total >= second.total) {
            return first;
        }
        else {
            return second;
        }
    }


    private RollResult min(RollResult first, RollResult second) {
        if (first.total <= second.total) {
            return first;
        }
        else {
            return second;
        }
    }



    public class RollResult {
        private int total;
        private int[] individualRolls;
        private boolean naddy20 = false;
        private boolean critFail = false;


        /*
         * Calculates the total
         */
        RollResult(int[] individualRolls) {
            this.individualRolls = individualRolls;
            calculateTotal();
        }


        /*
         * Adds up the rolls and adds the modifier (also checks for crits and fails)
         * TODO Optimisation is it better to do this when the die are rolled then pass it to reduce loops?
         */
        private void calculateTotal() {
            total = 0;
            for (int x : individualRolls) {
                total += x;

                if (x == 1) {
                    critFail = true;
                }
                else if (x == dieSize) {
                    naddy20 = true;
                }
            }

            total += modifier;
            if (total <= 0) {
                total = 1;
            }
        }


        public int getTotal() {
            return total;
        }


        public boolean isNaddy20() {
            return naddy20;
        }


        public boolean isCritFail() {
            return critFail;
        }


        public String toString() {
            final StringBuilder returnString = new StringBuilder(" rolled a ");
            returnString.append(total);

            if (critFail && naddy20) {
                returnString.append(" and managed to both crit and fumble!");
            }
            else if (critFail) {
                returnString.append(" and fumbled!");
            }
            else if (naddy20) {
                returnString.append(" with a nat 20!");
            }

            returnString.append("\n`");
            for (int x : individualRolls) {
                returnString.append(x);
                returnString.append(", ");
            }
            returnString.deleteCharAt(returnString.lastIndexOf(","));
            returnString.deleteCharAt(returnString.lastIndexOf(" "));
            returnString.append("`");

            return returnString.toString();
        }
    }
}
