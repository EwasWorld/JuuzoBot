package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class RemoveQuoteCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "removeQuote";
    }


    @Override
    public String getDescription() {
        return "removes a quote from the bot";
    }


    @Override
    public String getArguments() {
        return "{quote number}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.ADMIN;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        try {
            Quotes.removeQuote(Integer.parseInt(args));
            sendMessage(event.getChannel(), "Quote removed");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect quote number - it needs to be an integer");
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
