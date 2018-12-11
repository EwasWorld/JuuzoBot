package CommandsBox;

import CoreBox.Quotes;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * refactored: 13/11/18
 */
public class QuoteCommand extends AbstractCommand {
    /**
     * {@inheritDoc}
     */
    @Override
    CommandInterface[] getSecondaryCommands() {
        return QuoteArgument.values();
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
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
        executeSecondaryArgument(QuoteArgument.class, 2, args, event);
    }


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
                + "(NB: Juuzo has the memory of a goldfish and thus can only quote from the last 20 messages, be "
                + "quick! He also can't remember anything he himself has said, shame really)";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        final StringBuilder sb = new StringBuilder();
        for (QuoteArgument argument : QuoteArgument.values()) {
            sb.append(argument.getCommand());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    private enum QuoteArgument implements CommandInterface {
        ADD {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                sendMessage(event.getChannel(), Quotes.addQuote(args));
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Saves a quote to the system";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.USER;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{start of message}";
            }
        },
        GET {
            /**
             * {@inheritDoc}<br>
             * Prints the quote with the corresponding number (or random if no number is given) is returned
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
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


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Recalls a quote (randomly if no number is chosen)";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.USER;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "[quote number]";
            }
        },
        REMOVE {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
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


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Deletes a saved quote";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.ADMIN;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{quote number}";
            }
        },
        REINDEX {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                checkPermission(event.getMember(), Rank.ADMIN);
                Quotes.cleanQuoteIDs();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Resets all the quote numbers to be consecutive beginning from 1 again (useful if many quotes "
                        + "have been deleted making the numbers sparse)";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.ADMIN;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "";
            }
        },
        DELETE_ALL {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                checkPermission(event.getMember(), Rank.ADMIN);
                // TODO Implement "Thumbs up to this message if you're sure"
                Quotes.clearMessagesAndQuotes();
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Deletes all the quotes in the system and all messages currently saved (cannot save any "
                        + "messages from before sending this command)";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.ADMIN;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "";
            }
        };


        /**
         * {@inheritDoc}
         */
        @Override
        public String getCommand() {
            return this.toString().toLowerCase().replaceAll("_", " ");
        }
    }
}
