package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Logger;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class ClearLogCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "clearLog";
    }


    @Override
    public String getDescription() {
        return "clears all logged errors";
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

        Logger.clearLog();
        channel.sendMessage("Log cleared").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
