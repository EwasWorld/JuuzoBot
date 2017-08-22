package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import Foo.AbstractCommand;
import Foo.Quotes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class RemoveQuoteCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }


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
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        try {
            Quotes.removeQuote(Integer.parseInt(args));
            channel.sendMessage("Quote removed").queue();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect quote number - it needs to be an integer");
        }
    }
}
