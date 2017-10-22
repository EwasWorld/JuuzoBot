package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Logger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



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
        return HelpCommand.HelpVisibility.ADMIN;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        Logger.clearLog();
        sendMessage(event.getChannel(), "Log cleared");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
