package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.TimeZone;



public class GetNextGameCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "nextGame";
    }


    @Override
    public String getDescription() {
        return "prints time of your next game session";
    }


    @Override
    public String getArguments() {
        return "[timezone]";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        if (args.equals("")) {
            sendMessage(event.getChannel(), SessionTimes.getNextSessionAsString(event.getMember()));
        }
        else {
            sendMessage(
                    event.getChannel(),
                    SessionTimes.getNextSessionAsString(TimeZone.getTimeZone(args), event.getMember())
            );
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
