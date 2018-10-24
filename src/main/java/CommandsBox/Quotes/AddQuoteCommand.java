package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



// TODO Optimisation dump Quotes stuff in here as an inner class? And have just one QuotesCommand
public class AddQuoteCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "addQuote";
    }


    @Override
    public String getDescription() {
        return "adds a quote to the bot to be preserved forever ｡◕‿◕｡✿ "
                + "(NB: Juuzo has the memory of a goldfish and thus can only quote from the last 20 messages)";
    }


    @Override
    public String getArguments() {
        return "{start of message}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), Quotes.addQuote(args));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}