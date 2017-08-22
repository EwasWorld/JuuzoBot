package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import Foo.AbstractCommand;
import Foo.Quotes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GetQuoteCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(Quotes.getQuote(args)).queue();
    }
}
