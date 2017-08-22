package CommandsBox.Quotes;

import Foo.AbstractCommand;
import Foo.Quotes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class AddQuoteCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        channel.sendMessage(Quotes.addQuote(args)).queue();
    }
}