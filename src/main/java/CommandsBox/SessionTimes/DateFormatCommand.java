package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.Database;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class DateFormatCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "dateFormat";
    }


    @Override
    public String getDescription() {
        return "shows the format dates must be entered in";
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

        sendMessage(event.getChannel(), Database.setDateFormatStr);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.DM;
    }
}
