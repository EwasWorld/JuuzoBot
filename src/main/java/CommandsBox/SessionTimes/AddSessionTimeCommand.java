package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import Foo.AbstractCommand;
import Foo.Help;
import Foo.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class AddSessionTimeCommand extends AbstractCommand {
    private static final String dateFormatHelp =
                   "e.g. '16:00 21/8/17 BST' **or** '16:00 21/8/17 GMT + 1' (spaces around '+' are important)";

    @Override
    public Rank getRequiredRank() {
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
        return "{HH:mm dd/M/yy z}" + dateFormatHelp;
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(SessionTimes.addSessionTime(author, args)).queue();
    }
}
