package CommandsBox.SessionTimes;

import Foo.AbstractCommand;
import Foo.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class RemoveGameCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.ADMIN;
    }


    @Override
    public String getCommand() {
        return "removeGame";
    }


    @Override
    public String getDescription() {
        return "prevents time sessions for a game from being added";
    }


    @Override
    public String getArguments() {
        return "{game}";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        SessionTimes.removeGame(args);
        channel.sendMessage("Game removed").queue();
    }
}
