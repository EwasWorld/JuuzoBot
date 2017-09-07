package CommandsBox.Quotes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Quotes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



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
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        try {
            Quotes.removeQuote(Integer.parseInt(args));
            channel.sendMessage("Quote removed").queue();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect quote number - it needs to be an integer");
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
