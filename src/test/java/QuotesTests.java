import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadUserInputException;
import CoreBox.Quotes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



/**
 * refactored: 19/11/18
 */
public class QuotesTests {
    @Before
    public void setup() {
        DatabaseTable.setTestMode();
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addMessage("TestAuthor", "AddQuote");
        Quotes.addMessage("TestAuthor", "TestQuote");
    }

    @After
    public void tearDown() {
        Quotes.clearMessagesAndQuotes();
    }


    @Test
    public void testAddMessage() {
    }


    @Test
    public void testAddQuotes() {
        Quotes.addQuote("Add");
        Assert.assertEquals(1, Quotes.size());

        boolean exceptionThrown = false;
        try {
            Quotes.addQuote("TestQuote");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertFalse(exceptionThrown);
        Assert.assertEquals(2, Quotes.size());

        exceptionThrown = false;
        try {
            Quotes.addQuote("NotAQuote");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertEquals(2, Quotes.size());
    }


    @Test
    public void testRemoveQuote() {
        Quotes.addQuote("Add");
        Assert.assertEquals(1, Quotes.size());

        boolean exceptionThrown = false;
        try {
            Quotes.removeQuote(3);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertEquals(1, Quotes.size());

        Quotes.removeQuote(1);
        Assert.assertEquals(0, Quotes.size());
    }


    @Test
    public void testGetQuote() {
        Quotes.addQuote("AddQuote");
        Assert.assertEquals(1, Quotes.size());
        Quotes.getQuote();
        Quotes.getQuote(1);

        boolean exceptionThrown = false;
        try {
            Quotes.getQuote(0);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            Quotes.getQuote(3);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        Assert.assertEquals(1, Quotes.size());
    }


    @Test
    public void testDeleteAllQuotes() {
        Quotes.addQuote("AddQuote");
        Quotes.addQuote("TestQuote");
        Assert.assertEquals(2, Quotes.size());

        Quotes.clearMessagesAndQuotes();
        Assert.assertEquals(0, Quotes.size());
    }
}
