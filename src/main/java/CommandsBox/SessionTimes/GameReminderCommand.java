package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import Foo.AbstractCommand;
import Foo.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GameReminderCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.DM;
    }


    @Override
    public String getCommand() {
        return "gameReminder";
    }


    @Override
    public String getDescription() {
        return "@ mentions the game role and displays the countdown";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(SessionTimes.getSessionReminder(author)).queue();
    }
}
