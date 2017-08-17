package Foo;

import java.util.Random;



public class Roll {
    public static final String invalidFormat
            = "Invalid input. Use '!roll [adv/dis] [quantity] d {size} [modifier]' e.g. '!roll adv 1d20+2'";



    private enum RollType {NORMAL, ADVANTAGE, DISADVANTAGE}



    private int quantity;
    private int dieSize;
    private int modifier;


    public Roll(int dieSize) {
        quantity = 1;
        this.dieSize = dieSize;
        modifier = 0;
    }


    public Roll(int quantity, int dieSize, int modifier) {
        this.quantity = quantity;
        this.dieSize = dieSize;
        this.modifier = modifier;
    }


    /*
     * Returns the roll for the person
     * message should be in the format [adv/dis] [quantity] d {size} [modifier]
     */
    public static String rollDieFromChatEvent(String message, String author) {
        int quantity;
        int dieSize;
        int modifier;
        String[] messageParts;

        message = message.replace(" ", "");
        if (!message.contains("d")) {
            throw new IllegalArgumentException(invalidFormat);
        }

        final RollType rollType = getRollType(message);
        if (rollType != RollType.NORMAL) {
            message = message.substring(3);
        }

        try {
            messageParts = message.split("d");
            quantity = getQuantity(messageParts[0]);
            message = messageParts[1];

            try {
                messageParts = splitDieAndModifierStrings(message);

                if (messageParts.length != 2) {
                    throw new IllegalArgumentException("Incorrect die size or modifier");
                }
                dieSize = Integer.parseInt(messageParts[0]);
                modifier = Integer.parseInt(messageParts[1]);
            } catch (IllegalStateException e) {
                dieSize = Integer.parseInt(message);
                modifier = 0;
            }

            return author + " " + new Roll(quantity, dieSize, modifier).getStringForRoll(rollType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(invalidFormat);
        }
    }


    private static RollType getRollType(String message) {
        if (message.startsWith("adv")) {
            return RollType.ADVANTAGE;
        }
        else if (message.startsWith("dis")) {
            return RollType.DISADVANTAGE;
        }
        else {
            return RollType.NORMAL;
        }
    }


    private static int getQuantity(String quantityStr) {
        if (quantityStr.length() != 0) {
            return Integer.parseInt(quantityStr);
        }
        else {
            return 1;
        }
    }


    private static String[] splitDieAndModifierStrings(String message) {
        if (message.contains("+") || message.contains("-")) {
            if (message.contains("+")) {
                message = message.replaceAll("\\+", "plus");
                return message.split("plus");
            }
            else {
                String[] messageParts = message.split("-");

                // Put the negative sign back in so that when the int is parsed it works correctly
                messageParts[1] = "-" + messageParts[1];
                return messageParts;
            }
        }
        else {
            throw new IllegalStateException("Incorrect die size and modifier");
        }
    }


    public static int quickRoll(int dieSize) {
        return new Roll(dieSize).roll().result;
    }


    private String getStringForRoll() {
        return getStringForRoll(RollType.NORMAL);
    }


    /*
     * Rolls the die and returns the output as a string (inc. crits and fumbles)
     */
    private String getStringForRoll(RollType rollType) {
        if (dieSize == 1) {
            // TODO roll a d1
            // quantity is number of bad things to happen or always just one thing?
            return "has incurred Eywa's wrath and is struck by a bolt of lightning";
        }
        else {
            final RollResult rollResult = roll(rollType);
            String returnString = "rolled a " + rollResult.getResult();

            if (rollResult.isCritFail() && rollResult.isNaddy20()) {
                return returnString + " and managed to both crit and fumble!";
            }
            else if (rollResult.isCritFail()) {
                return returnString + " and fumbled!";
            }
            else if (rollResult.isNaddy20()) {
                return returnString + " and crits!";
            }
            else {
                return returnString;
            }
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


    /*
     * Rolls the die
     */
    public RollResult roll() {
        boolean naddy20 = false;
        boolean critFail = false;

        if (dieSize > 1) {
            final Random random = new Random();
            int total = 0;

            for (int j = 0; j < quantity; j++) {
                int roll = random.nextInt(dieSize) + 1;
                total += roll;

                if (roll == dieSize) {
                    naddy20 = true;
                }
                if (roll == 1) {
                    critFail = true;
                }
            }
            total += modifier;

            if (total <= 0) {
                total = 1;
            }
            return new RollResult(total, naddy20, critFail);
        }
        else {
            throw new IllegalArgumentException("Invalid die size");
        }
    }


    private RollResult max(RollResult first, RollResult second) {
        if (first.result >= second.result) {
            return first;
        }
        else {
            return second;
        }
    }


    private RollResult min(RollResult first, RollResult second) {
        if (first.result <= second.result) {
            return first;
        }
        else {
            return second;
        }
    }


    public class RollResult {
        private int result;
        private boolean naddy20 = false;
        private boolean critFail = false;


        private RollResult(int result, boolean naddy20, boolean critFail) {
            this.result = result;
            this.naddy20 = naddy20;
            this.critFail = critFail;
        }


        public int getResult() {
            return result;
        }


        public boolean isNaddy20() {
            return naddy20;
        }


        public boolean isCritFail() {
            return critFail;
        }
    }
}
