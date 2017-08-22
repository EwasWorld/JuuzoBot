import ExceptionsBox.BadUserInputException;
import CoreBox.SessionTimes;
import junit.framework.TestCase;



public class SessionTimesTests extends TestCase {
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        SessionTimes.clearGameInformation();
    }


    public void testAddGame() {
        SessionTimes.addGame("TG Test Game");
        assertEquals(1, SessionTimes.size());
        
        boolean exceptionThrown = false;
        try {
            SessionTimes.addGame("TG Test Game 2");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(1, SessionTimes.size());
    }


    public void testAddSessionTime() {
        SessionTimes.addGame("TG Test Game");
        assertEquals(1, SessionTimes.size());

        // TODO How to test without passing a member object
    }


    public void testGetSession() {
        // TODO
    }


    public void testGetSessionReminder() {
        // TODO
    }


    public void testRemoveGame() {
        // TODO
    }
}
