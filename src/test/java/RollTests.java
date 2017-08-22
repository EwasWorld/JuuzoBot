import ExceptionsBox.BadUserInputException;
import Foo.Roll;
import junit.framework.TestCase;



public class RollTests extends TestCase {
    public void testQuickRollDie() {
        Roll.quickRoll(10);
    }


    public void testStringRollJustSize() {
        Roll.rollDieFromChatEvent("d8", "TestAuthor");
    }


    public void testStringRollQuantitySize() {
        Roll.rollDieFromChatEvent("2d8", "TestAuthor");
    }


    public void testStringRollQuantitySizeModifier() {
        Roll.rollDieFromChatEvent("2d8+1", "TestAuthor");
    }


    public void testStringRollSizeModifier() {
        Roll.rollDieFromChatEvent("d8+1", "TestAuthor");
    }


    public void testBadStringRolls() {
        boolean exceptionThrown = false;
        try {
            Roll.rollDieFromChatEvent("", "TestAuthor");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);


        exceptionThrown = false;
        try {
            Roll.rollDieFromChatEvent("8", "TestAuthor");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);


        exceptionThrown = false;
        try {
            Roll.rollDieFromChatEvent("d", "TestAuthor");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);


        exceptionThrown = false;
        try {
            Roll.rollDieFromChatEvent("a 2d8+1", "TestAuthor");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }


    public void testCreateRollAndGetResult() {
        new Roll(8).roll();
        new Roll(8).getStringForRoll();
        new Roll(2, 8, 1).roll();
        new Roll(2, 8, 1).getStringForRoll();
    }
}
