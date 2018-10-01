package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class GetQuoteCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "getQuote";
    }


    @Override
    public String getDescription() {
        return "[quote number]";
    }


    @Override
    public String getArguments() {
        return "retrieves the desired quote (or a random one if no number is given)";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    /*
     * Quote with the corresponding number is returned
     * If the string is empty a random quote is returned
     */
    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());
        /* TODO Refactor base method
        String returnMessage;
        if (args.equals("")) {
            returnMessage = Quotes.getQuote();
        }
        else {
            try {
                returnMessage = Quotes.getQuote(Integer.parseInt(args));
            } catch (IllegalArgumentException e) {
                throw new BadUserInputException("Incorrect quote format, either give no argument or an integer");
            }
        }

        sendMessage(event.getChannel(), returnMessage);*/
        sendMessage(event.getChannel(), "Function not currently working");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
