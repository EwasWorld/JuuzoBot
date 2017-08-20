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
                    throw new IllegalArgumentException("Quote is ambiguous, please clarify");
                }
            }
        }

        if (quoteToAdd == null) {
            throw new IllegalArgumentException("Quote not found");
        }
        else {
            quotes.add(quoteToAdd);
            return getQuote(quotes.size() - 1);
        }
    }


    /*
     * Gets a specific quote
     */
    public static String getQuote(int index) {
        if (quotes.size() == 0) {
            throw new IllegalArgumentException("There are no saved quotes");
        }
        else if (index >= quotes.size()) {
            throw new IllegalArgumentException("Quote number is too high");
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
            throw new IllegalStateException("There are no saved quotes");
        }
    }


    /*
     * Removes the specified quote from the bot
     */
    public static void removeQuote(int index) {
        getQuote(index);
        quotes.remove(index);
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
