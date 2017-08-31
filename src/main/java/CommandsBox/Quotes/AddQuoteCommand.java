package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(Quotes.addQuote(args)).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}