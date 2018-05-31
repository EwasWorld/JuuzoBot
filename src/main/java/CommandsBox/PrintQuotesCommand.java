package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class PrintQuotesCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "printQuotes";
    }


    @Override
    public String getDescription() {
        return null;
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.ADMIN;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        System.out.println("");
        int i = 0;
        while (true) {
            try {
                System.out.println(Quotes.getQuote(i));
                System.out.println("");
            } catch (BadUserInputException e) {
                break;
            }
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
