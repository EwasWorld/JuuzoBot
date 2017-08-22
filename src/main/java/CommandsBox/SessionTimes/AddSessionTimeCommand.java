package CommandsBox.SessionTimes;

import Foo.AbstractCommand;
import Foo.Help;
import Foo.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class AddSessionTimeCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.DM;
    }


    @Override
    public String getCommand() {
        return "addSessionTime";
    }


    @Override
    public String getDescription() {
        return "updates the next session time (see !dateFormat for help)";
    }


    @Override
    public String getArguments() {
        return "{HH:mm dd/M/yy z}\n" + Help.dateFormatHelp;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(SessionTimes.addSessionTime(author, args)).queue();
    }
}
