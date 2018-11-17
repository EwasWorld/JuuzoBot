package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



/**
 * refactored: 13/11/18
 */
public class QuoteCommand extends AbstractCommand {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "quote";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Stores quote to the bot to be preserved forever ｡◕‿◕｡✿ "
                + "(NB: Juuzo has the memory of a goldfish and thus can only quote from the last 20 messages)";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        return "add {start of message} / get [quote number] / remove {quote number} / reindex / deleteAND all";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    /**
     * {@inheritDoc}<br>
     */
    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());
        executeSecondaryArgument(QuoteArgument.class, args, event);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    private enum QuoteArgument implements SecondaryCommandAction {
        ADD {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                sendMessage(event.getChannel(), Quotes.addQuote(args));
            }
        },
        GET {
            /**
             * {@inheritDoc}<br>
             * Prints the quote with the corresponding number (or random if no number is given) is returned
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                String returnMessage;
                if (args.equals("")) {
                    returnMessage = Quotes.getQuote();
                }
                else {
                    int quoteNumber;
                    try {
                        quoteNumber = Integer.parseInt(args);
                    } catch (IllegalArgumentException e) {
                        throw new BadUserInputException(
                                "Incorrect quote format, either give no argument or an integer");
                    }
                    if (quoteNumber == 0) {
                        throw new BadUserInputException("Arrays start at 1 ( ͡° ͜ʖ ͡°)");
                    }
                    else {
                        returnMessage = Quotes.getQuote(quoteNumber);
                    }
                }

                sendMessage(event.getChannel(), returnMessage);
            }
        },
        REMOVE {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                checkPermission(event.getMember(), Rank.ADMIN);
                int quoteNumber;
                try {
                    quoteNumber = Integer.parseInt(args);
                } catch (IllegalArgumentException e) {
                    throw new BadUserInputException("Incorrect quote format, either give no argument or an integer");
                }
                Quotes.removeQuote(quoteNumber);
                sendMessage(event.getChannel(), "Quote removed");
            }
        },
        REINDEX {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                checkPermission(event.getMember(), Rank.ADMIN);
                Quotes.cleanQuoteIDs();
            }
        },
        DELETEALL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                checkPermission(event.getMember(), Rank.ADMIN);
                // TODO Implement "Thumbs up to this message if you're sure"
                Quotes.clearMessagesAndQuotes();
            }
        }
    }
}
