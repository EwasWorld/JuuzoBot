package Foo;

import DataPersistenceBox.DataPersistence;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class Quotes implements Serializable {
    private static final String fileName = "Quotes.txt";
    private static Quotes[] channelMessages = new Quotes[20];
    private static List<Quotes> quotes = new ArrayList<>();
    private static int head = 0;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm z");
    private String author;
    private ZonedDateTime date;
    private String message;


    public Quotes(String author, ZonedDateTime date, String message) {
        this.author = author;
        this.date = date;
        this.message = message;
    }


    /*
     * Temporarily stores a channel message
     */
    public static void addMessage(String author, String contents) {
        channelMessages[head] = new Quotes(author, ZonedDateTime.now(), contents);
        head = ++head % channelMessages.length;
    }


    /*
     * Finds the specified message and stores for the future
     */
    public static String addQuote(String searchMessage) {
        Quotes quoteToAdd = null;
        for (Quotes quote : channelMessages) {
            if (quote != null && quote.message.startsWith(searchMessage)) {
                if (quoteToAdd == null) {
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
        else {
            quotes.add(quoteToAdd);
            return getQuote(quotes.size() - 1);
        }
    }


    /*
     * If a number is given in the string then the quote with the corresponding number is returned
     * If the string is empty a random quote is returned
     */
    public static String getQuote(String index) {
        if (index.equals("")) {
            return Quotes.getQuote();
        }
        else {
            try {
                return Quotes.getQuote(Integer.parseInt(index));
            } catch (IllegalArgumentException e) {
                throw new BadUserInputException("Incorrect quote format, either give no argument or an integer");
            }
        }
    }


    /*
     * Gets a specific quote
     */
    public static String getQuote(int index) {
        if (quotes.size() == 0) {
            throw new BadStateException("There are no saved quotes");
        }
        else if (index >= quotes.size()) {
            throw new BadUserInputException("Quote number is too high");
        }
        else {
            final Quotes quote = quotes.get(index);
            return String.format(
                    "Quote number %d\n**%s** - %s\n*%s*", index,
                    quote.author, dateTimeFormatter.format(quote.date), quote.message
            );
        }
    }


    /*
     * Gets a random quote
     */
    public static String getQuote() {
        if (quotes.size() != 0) {
            return getQuote(new Random().nextInt(quotes.size()));
        }
        else {
            throw new BadStateException("There are no saved quotes");
        }
    }


    /*
     * Removes the specified quote from the bot
     */
    public static void removeQuote(int index) {
        getQuote(index);
        quotes.remove(index);
    }


    public static int size() {
        return quotes.size();
    }


    public static void clearMessagesAndQuotes() {
        channelMessages = new Quotes[20];
        quotes = new ArrayList<>();

    }


    public static void save() {
        try {
            DataPersistence.save(fileName, quotes);
        } catch (IllegalStateException e) {
            System.out.println("Quotes save failed");
        }
    }


    public static void load() {
        try {
            quotes = (List<Quotes>) DataPersistence.loadFirstObject(fileName);
        } catch (IllegalStateException e) {
            System.out.println("Quotes load failed");
        }
    }
}
