package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ShowDatabaseCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "showDB";
    }


    @Override
    public String getDescription() {
        return null;
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

        String showDBString = SessionDatabase.databaseToString();
        if (showDBString.equals("")) {
            showDBString = "There are no games in the database";
        }

        sendMessage(event.getChannel(), showDBString);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
