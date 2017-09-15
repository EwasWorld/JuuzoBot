package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Quotes;
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


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), Quotes.getQuote(args));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
