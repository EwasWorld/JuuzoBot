import CoreBox.GameSession;
import CoreBox.IDs;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;



/**
 * refactored 22/11/18
 */
public class GameSessionTests {
    @Before
    public void setup() {
        DatabaseTable.setTestMode();
    }

    @After
    public void tearDown() {
        GameSession.getDatabaseWrapper().deleteAllTables();
    }


    @Test
    public void testAddGame() {
        GameSession.addGameToDatabase("POP", "The Particulars of Petrification", IDs.eywaID);
        Assert.assertTrue(GameSession.checkRowCounts(1, 0));

        boolean exceptionThrown = false;
        try {
            GameSession.addGameToDatabase("POP", "cbfghgd", "tfhdsefghfj");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertTrue(GameSession.checkRowCounts(1, 0));

        GameSession.addGameToDatabase("YE", "Yell Exclusively", IDs.eywaID);
        Assert.assertTrue(GameSession.checkRowCounts(2, 0));
    }


    @Test
    public void testAddPlayer() {
        GameSession.addGameToDatabase("POP", "The Particulars of Petrification", IDs.eywaID);
        Assert.assertTrue(GameSession.checkRowCounts(1, 0));

        GameSession.addPlayer("POP", "Player1");
        GameSession.addPlayer("POP", "Player2");
        Assert.assertTrue(GameSession.checkRowCounts(1, 2));


        boolean exceptionThrown = false;
        try {
            GameSession.addPlayer("POP", "Player2");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertTrue(GameSession.checkRowCounts(1, 2));
    }


    @Test
    public void testAddSessionTime() {
        // Find using DM
        boolean exceptionThrown = false;
        try {
            GameSession.addSessionTime(IDs.eywaID, ZonedDateTime.now());
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        GameSession.addGameToDatabase("POP", "The Particulars of Petrification", IDs.eywaID);
        GameSession.addSessionTime(IDs.eywaID, ZonedDateTime.now());
        GameSession.addGameToDatabase("YE", "Yell Exclusively", IDs.eywaID);

        exceptionThrown = false;
        try {
            GameSession.addSessionTime(IDs.eywaID, ZonedDateTime.now());
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Find using short name
        GameSession.addGameToDatabase("Hella", "Hella Gud", "Heh");
        GameSession.addSessionTime(IDs.eywaID, "POP", ZonedDateTime.now());
        GameSession.addSessionTime(IDs.eywaID, "YE", ZonedDateTime.now());

        exceptionThrown = false;
        try {
            GameSession.addSessionTime(IDs.eywaID, "ouisidgf", ZonedDateTime.now());
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        exceptionThrown = false;
        try {
            GameSession.addSessionTime(IDs.eywaID, "Hella", ZonedDateTime.now());
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }


    @Test
    public void testGetAllSessions() {
        GameSession.addGameToDatabase("POP", "The Particulars of Petrification", IDs.eywaID);
        GameSession.addSessionTime(IDs.eywaID, ZonedDateTime.now());
        GameSession.addGameToDatabase("YE", "Yell Exclusively", "Bloop");
        GameSession.addGameToDatabase("Hella", "Hella Gud", "Heh");
        GameSession.addPlayer("POP", "Player1");
        GameSession.addPlayer("POP", "Player2");
        GameSession.addPlayer("YE", "Player2");
        System.out.println(GameSession.getAllSessionTimes("Player1"));
        System.out.println("--------------------------");
        System.out.println(GameSession.getAllSessionTimes("Player2"));
        System.out.println("--------------------------");
        System.out.println(GameSession.getAllSessionTimes(IDs.eywaID));

        boolean exceptionThrown = false;
        try {
            System.out.println(GameSession.getAllSessionTimes("sdufhjskldjg"));
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }


    @Test
    public void testGetNextSession() {
        String shortName = "POP";
        GameSession.addGameToDatabase(shortName, "The Particulars of Petrification", IDs.eywaID);
        // No session time
        boolean exceptionThrown = false;
        try {
            GameSession.getNextSession(shortName);
        } catch (BadStateException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Session time in the past
        ZonedDateTime zonedDateTime = ZonedDateTime.now().minusDays(1);
        GameSession.addSessionTime(IDs.eywaID, zonedDateTime);
        exceptionThrown = false;
        try {
            GameSession.getNextSession(shortName);
        } catch (BadStateException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        // Session time in the future
        zonedDateTime = zonedDateTime.plusDays(3);
        GameSession.addSessionTime(IDs.eywaID, zonedDateTime);
        // Note: game times can only be input up to minute accuracy
        Assert.assertEquals(zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(MINUTES), GameSession
                .getNextSession(shortName));

        // Invalid shortname
        exceptionThrown = false;
        try {
            GameSession.getNextSession("sdufhjskldjg");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

    }


    @Test
    public void testGetSessionReminder() {
        // TODO dm with only one game
        // TODO specified shortname
    }


    @Test
    public void testDeleteGame() {
        GameSession.addGameToDatabase("POP", "The Particulars of Petrification", IDs.eywaID);
        GameSession.addGameToDatabase("YE", "Yell Exclusively", "Bloop");
        GameSession.addGameToDatabase("Hella", "Hella Gud", "Heh");
        GameSession.addPlayer("POP", "Player1");
        GameSession.addPlayer("POP", "Player2");
        GameSession.addPlayer("YE", "Player2");
        Assert.assertTrue(GameSession.checkRowCounts(3, 3));

        GameSession.deleteGame("POP");
        Assert.assertTrue(GameSession.checkRowCounts(2, 1));

        boolean exceptionThrown = false;
        try {
            GameSession.deleteGame("sdgdfhdfg");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertTrue(GameSession.checkRowCounts(2, 1));
    }


    @Test
    public void testGamesList() {
        GameSession.addGameToDatabase("POP", "The Particulars of Petrification", IDs.eywaID);
        GameSession.addGameToDatabase("YE", "Yell Exclusively", "Bloop");
        GameSession.addGameToDatabase("Hella", "Hella Gud", "Heh");
        System.out.println(GameSession.getGamesList());
    }
}
