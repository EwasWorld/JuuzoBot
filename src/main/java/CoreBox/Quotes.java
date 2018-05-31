package CoreBox;

import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



/*
 * Preserve the beautiful words of those in the server in time immemorial
 * Allows users to save messages and retrieve saved messages
 */
public class Quotes implements Serializable {
    private static final String fileName = "Quotes.txt";
    private static List<Quotes> savedQuotes = new ArrayList<>();
    private static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ofPattern(Database.setDateFormatStr);
    private String author;
    private ZonedDateTime date;
    private String message;


    public Quotes(String author, ZonedDateTime date, String message) {
        this.author = author;
        this.date = date;
        this.message = message;
    }


    public static void addMessage(String author, String contents) {
        MesssageHolder.addMessage(author, contents);
    }


    /*
     * Saves the the specified message
     */
    public static String addQuote(String searchMessage) {
        savedQuotes.add(MesssageHolder.findQuote(searchMessage));
        return getQuote(savedQuotes.size() - 1);
    }


    /*
     * Returns the specific quote as a string of author, time, date
     */
    public static String getQuote(int index) {
        if (savedQuotes.size() == 0) {
            throw new BadStateException("There are no saved quotes");
        }
        else if (index >= savedQuotes.size()) {
            throw new BadUserInputException("Quote number is too high");
        }
        else {
            final Quotes quote = savedQuotes.get(index);
            return String.format("Quote number %d\n%s", index, quote.toString());
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


    /*
     * Gets a random quote
     */
    public static String getQuote() {
        if (savedQuotes.size() == 0) {
            throw new BadStateException("There are no saved quotes");
        }
        return getQuote(new Random().nextInt(savedQuotes.size()));
    }


    /*
     * Deletes the specified saved quote from the bot
     */
    public static void removeQuote(int index) {
        getQuote(index);
        savedQuotes.remove(index);
    }


    public static int size() {
        return savedQuotes.size();
    }


    /*
     * WARNING: DATA LOSS
     * Deletes the channelMessages history and all savedQuotes
     */
    public static void clearMessagesAndQuotes() {
        MesssageHolder.clearMessages();
        savedQuotes = new ArrayList<>();
    }


    public static void save() {
        try {
            DataPersistence.save(fileName, savedQuotes);
        } catch (IllegalStateException e) {
            System.out.println("Quotes save failed");
        }
    }


    public static void load() {
        try {
            savedQuotes = (List<Quotes>) DataPersistence.loadFirstObject(fileName);
        } catch (IllegalStateException e) {
            System.out.println("Quotes load failed");
        }
    }


    private static class MesssageHolder {
        private static final int numberOfMessagesToHold = 20;
        // Holds onto a specified number of channel messages which quotes can be saved from
        // If it runs out of space it overwrites the oldest quote
        private static Quotes[] channelMessages = new Quotes[numberOfMessagesToHold];
        // The location in channelMessages which is to be written to next
        private static int head = 0;


        /*
         * Temporarily stores a given channel message with the current time
         */
        static void addMessage(String author, String contents) {
            channelMessages[head] = new Quotes(author, ZonedDateTime.now(), contents);
            head = ++head % channelMessages.length;
        }


        /*
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
