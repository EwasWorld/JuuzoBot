import ExceptionsBox.BadUserInputException;
import CoreBox.Quotes;
import junit.framework.TestCase;



public class QuotesTests extends TestCase {
    /*@Override
    public void tearDown() throws Exception {
        super.tearDown();
        Quotes.clearMessagesAndQuotes();
    }


    public void testAddMessage() {
        Quotes.addMessage("TestAuthor", "TestQuote");
    }


    public void testAddQuotes() {
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addMessage("TestAuthor", "AddQuote");
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addQuote("Add");
        assertEquals(1, Quotes.size());


        boolean exceptionThrown = false;
        try {
            Quotes.addQuote("TestQuote");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(1, Quotes.size());


        exceptionThrown = false;
        try {
            Quotes.addQuote("NotAQuote");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(1, Quotes.size());
    }


    public void testRemoveQuote() {
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addMessage("TestAuthor", "AddQuote");
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addQuote("Add");
        assertEquals(1, Quotes.size());


        boolean exceptionThrown = false;
        try {
            Quotes.removeQuote(3);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(1, Quotes.size());


        Quotes.removeQuote(0);
        assertEquals(0, Quotes.size());
    }


    public void testGetQuote() {
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addMessage("TestAuthor", "AddQuote");
        Quotes.addMessage("TestAuthor", "TestQuote");
        Quotes.addQuote("AddQuote");
        assertEquals(1, Quotes.size());

        Quotes.getQuote();
        Quotes.getQuote(0);


        boolean exceptionThrown = false;
        try {
            Quotes.getQuote(3);
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);


        exceptionThrown = false;
        try {
            Quotes.getQuote("3");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);


        exceptionThrown = false;
        try {
            Quotes.getQuote("Potato");
        } catch (BadUserInputException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }*/
}
