package CommandsBox.SessionTimes;

import Foo.AbstractCommand;
import Foo.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class AddGameCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.ADMIN;
    }


    @Override
    public String getCommand() {
        return "addGame";
    }


    @Override
    public String getDescription() {
        return "allows time sessions for a game to be added";
    }


    @Override
    public String getArguments() {
        return "{role} {full name}";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        SessionTimes.addGame(args);
        channel.sendMessage("Game added").queue();
    }
}
