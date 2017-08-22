package CommandsBox.SessionTimes;

import Foo.AbstractCommand;
import Foo.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GetNextGameCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "nextGame";
    }


    @Override
    public String getDescription() {
        return "prints time of your next game session";
    }


    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(
                SessionTimes.getNextSessionAsString(author)
        ).queue();
    }
}
