package CoreBox;

import DatabaseBox.DatabaseFieldArgs;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.FeatureUnavailableException;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;



/**
 * Preserve the beautiful words of those in the server in time immemorial
 * Allows users to save messages and retrieve saved messages
 * TODO Implement Remove duplicate quotes
 * refactored: 27/09/18
 */
public class Quotes implements Serializable {
    private static DatabaseTable databaseTable = createDatabaseTable();
    private static final String AUTHOR = "author";
    private static final String DATE = "date";
    private static final String MESSAGE = "message";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Database.setDateFormatStr);
    private String author;
    private ZonedDateTime date;
    private String message;


    public Quotes(String author, ZonedDateTime date, String message) {
        this.author = author;
        this.date = date;
        this.message = message;
    }


    /**
     * Set up the table in which the quotes will be saved
     */
    private static DatabaseTable createDatabaseTable() {
        Map<String, DatabaseFieldArgs> fields = new HashMap<>();
        fields.put(AUTHOR, new DatabaseFieldArgs(DatabaseTable.SQLType.TEXT, true));
        fields.put(DATE, new DatabaseFieldArgs(DatabaseTable.SQLType.DATE, true));
        fields.put(MESSAGE, new DatabaseFieldArgs(DatabaseTable.SQLType.TEXT, true));
        return new DatabaseTable("Quotes", fields);
    }


    /**
     * Add a message to the message queue so that it can be found later if someone tries to quote it
     */
    public static void addMessage(String author, String contents) {
        MessageHolder.addMessage(author, contents);
    }


    /**
     * Saves the specified message (matched using startsWith)
     */
    public static String addQuote(String searchMessage) {
        Quotes quote = MessageHolder.findQuote(searchMessage);

        Map<String, Object> args = new HashMap<>();
        args.put(AUTHOR, quote.author);
        args.put(DATE, quote.date);
        args.put(MESSAGE, quote.message);
        databaseTable.insert(args);

        return getQuote(size());
    }


    /**
     * @return the specific quote as a string of author, time, date
     */
    public static String getQuote(int index) {
        int size = size();
        if (size == 0) {
            throw new BadStateException("There are no saved quotes");
        }
        else if (index > size) {
            throw new BadUserInputException("Quote number is too high");
        }
        else {
            Map<String, Object> args = new HashMap<>();
            args.put(databaseTable.getPrimaryKey(), index);
            DatabaseTable.ResultsSetAction resultsSetAction = rs -> {
                if (rs.next()) {
                    String author = rs.getString(AUTHOR);
                    ZonedDateTime date = DatabaseTable.getDatabaseDateFromString(rs.getString(DATE));
                    String message = rs.getString(MESSAGE);
                    return new Quotes(author, date, message);
                }
                return null;
            };
            final Quotes quote = (Quotes) databaseTable.selectAND(args, resultsSetAction);
            if (quote != null) {
                return String.format("Quote number %d\n%s", index, quote.toString());
            }
            else {
                throw new BadStateException("There is no quote with the id " + index);
            }
        }
    }


    public String toString() {
        final String printString;
        if (message.startsWith("*") || message.endsWith("*")) {
            printString = message;
        }
        else {
            printString = String.format("*%s*", message);
        }

        return String.format("**%s** - %s\n*%s*", author, dateTimeFormatter.format(date), printString);
    }


    /**
     * @return a random quote
     */
    public static String getQuote() {
        if (size() == 0) {
            throw new BadStateException("There are no saved quotes");
        }
        return getQuote(new Random().nextInt(size()) + 1);
    }


    /**
     * TODO Refactor
     * Deletes the specified saved quote from the bot
     */
    public static void removeQuote(int index) {
        getQuote(index);
        Map<String, Object> args = new HashMap<>();
        args.put(databaseTable.getPrimaryKey(), index);
        databaseTable.delete(args);
    }


    /**
     * @return the number of quotes currently stored
     */
    public static int size() {
        return databaseTable.getRowCount();
    }


    /**
     * WARNING: DATA LOSS
     * Deletes the channelMessages history and all savedQuotes
     */
    public static void clearMessagesAndQuotes() {
        MessageHolder.clearMessages();
        databaseTable.deleteTable();
    }


    /**
     * Resets all the quoteIDs so that they are consecutive stating from 1 again
     * Used when the quotesIDs become very sparse such that getting a random quote often results in failure
     */
    public static void cleanQuoteIDs() {
        throw new FeatureUnavailableException(
                "Reset all the quoteIDs so that they are consecutive stating from 1 (helpful if many quotes have been"
                        + " deleted and numbers are getting sparse)");
    }


    /**
     * Stores the most recent channel messages the bot has picked up
     */
    private static class MessageHolder {
        private static final int numberOfMessagesToHold = 20;
        // Holds onto a specified number of channel messages which quotes can be saved from
        // If it runs out of space it overwrites the oldest quote
        private static Quotes[] channelMessages = new Quotes[numberOfMessagesToHold];
        // The location in channelMessages which is to be written to next
        private static int head = 0;


        /**
         * Temporarily stores a given channel message with the current time
         */
        static void addMessage(String author, String contents) {
            channelMessages[head] = new Quotes(author, ZonedDateTime.now(), contents);
            head = ++head % channelMessages.length;
        }


        /**
         * Returns the quote that begins with what is given
         */
        static Quotes findQuote(String searchMessage) {
            Quotes quoteToAdd = null;
            for (Quotes quote : channelMessages) {
                if (quote != null && quote.message.startsWith(searchMessage)) {
                    if (quoteToAdd == null || quoteToAdd.message.equalsIgnoreCase(quote.message)) {
                        quoteToAdd = quote;
                    }
                    else {
                        throw new BadUserInputException("Quote is ambiguous, please clarify");
                    }
                }
            }

            if (quoteToAdd == null) {
                throw new BadUserInputException("Quote not found");
            }

            return quoteToAdd;
        }


        static void clearMessages() {
            channelMessages = new Quotes[numberOfMessagesToHold];
        }
    }
}
