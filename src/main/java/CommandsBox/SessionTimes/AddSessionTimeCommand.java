package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class AddSessionTimeCommand extends AbstractCommand {
    private static final String dateFormatHelp =
            "e.g. '16:00 21/8/17 BST' **or** '16:00 21/8/17 GMT + 1' (spaces around '+' are important)";


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
        return "{HH:mm dd/M/yy z} " + dateFormatHelp;
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), SessionTimes.addSessionTime(event.getMember(), args));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.DM;
    }
}
