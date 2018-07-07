package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;



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

        // TODO
//        SessionDatabase.deleteTables();
        sendMessage(event.getChannel(), "Tables cleared");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
