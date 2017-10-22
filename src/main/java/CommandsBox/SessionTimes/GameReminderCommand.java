package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class GameReminderCommand extends AbstractCommand {
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
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        // TODO SessionDatabase
        sendMessage(event.getChannel(), SessionTimes.getSessionReminder(event.getMember()));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.DM;
    }
}
