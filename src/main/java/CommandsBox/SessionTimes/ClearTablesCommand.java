package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ClearTablesCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "clearSessions";
    }


    @Override
    public String getDescription() {
        return "deletes all session data";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        SessionDatabase.clearTables();
        sendMessage(event.getChannel(), "Tables cleared");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
