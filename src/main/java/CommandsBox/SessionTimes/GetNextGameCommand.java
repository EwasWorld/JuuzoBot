package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Date;
import java.util.Map;



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
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    // TODO Implement add countdown for nearest game
    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        Map<String, Date> map = SessionDatabase.getNextSessionTime(event.getAuthor().getId());

        StringBuilder stringBuilder = new StringBuilder("");
        if (args.equals("")) {
            while (map.size() > 0) {
                String stringForDate = null;
                Date nearestFutureDate = null;
                for (String shortName : map.keySet()) {
                    if (nearestFutureDate == null || nearestFutureDate.after(map.get(shortName))) {
                        stringForDate = shortName;
                        nearestFutureDate = map.get(shortName);
                    }
                }
                stringBuilder
                        .append(stringForDate + " " + SessionDatabase.printDateFormat.format(nearestFutureDate) + "\n");
            }
        }
        else {
            if (map.containsKey(args.toUpperCase())) {
                stringBuilder.append(map.get(args.toUpperCase()));
            }
            else {
                throw new BadUserInputException("Invalid short game name");
            }
        }

        sendMessage(event.getChannel(), stringBuilder.toString());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
