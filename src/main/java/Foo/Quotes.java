package Foo;

import net.dv8tion.jda.core.entities.MessageChannel;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class Quotes implements Serializable {
    private static final String fileLocation = IDs.mainFilePath + "Foo/Quotes.txt";
    private static Quotes[] channelMessages = new Quotes[20];
    private static List<Quotes> quotes = new ArrayList<>();
    private static int head = 0;
    private String author;
    private ZonedDateTime date;
    private String message;
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm z");


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
    public static void addQuote(MessageChannel channel, String searchMessage) {
        Quotes quoteToAdd = null;
        for (Quotes quote : channelMessages) {
            if (quote != null && quote.message.startsWith(searchMessage)) {
                if (quoteToAdd == null) {
                    quoteToAdd = quote;
                }
                else {
                    channel.sendMessage("Quote is ambiguous, please clarify").queue();
                    return;
                }
            }
        }

        if (quoteToAdd == null) {
            channel.sendMessage("Quote not found").queue();
        }
        else {
            quotes.add(quoteToAdd);
            channel.sendMessage(getQuote(quotes.size() - 1)).queue();
        }
    }

    /*
     * Gets a random quote
     */
    public static void getQuote(MessageChannel channel) {
        if (quotes.size() == 0) {
            channel.sendMessage(getQuote(new Random().nextInt(quotes.size()))).queue();
        }
        else {
            channel.sendMessage("There are no saved quotes").queue();
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
    public static void getQuote(MessageChannel channel, int index) {
        try {
            channel.sendMessage(getQuote(index)).queue();
        } catch (IllegalArgumentException e) {
            channel.sendMessage(e.getMessage()).queue();
        }
    }

    /*
     * Removes the specified quote from the bot
     */
    public static void removeQuote(MessageChannel channel, int index) {
        try {
            getQuote(index);
            quotes.remove(index);
            channel.sendMessage("Quote removed").queue();
        } catch (IllegalArgumentException e) {
            channel.sendMessage(e.getMessage()).queue();
        }
    }

    public static void save() {
        try {
            LoadSaveConstants.save(fileLocation, quotes);
        } catch (IllegalStateException e) {
            System.out.println("Quotes save failed");
        }
    }

    public static void load() {
        try {
            quotes = (List<Quotes>) LoadSaveConstants.loadFirstObject(fileLocation);
        } catch (IllegalStateException e) {
            System.out.println("Quotes load failed");
        }
    }
}
