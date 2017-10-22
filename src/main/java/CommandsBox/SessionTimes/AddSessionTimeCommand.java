package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import CoreBox.SessionTimes;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.util.Date;



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
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        Date date;
        String sessionName;

        try {
            date = SessionTimes.setDateFormat.parse(args);
            sessionName = SessionDatabase.addSessionTime(event.getAuthor().getId(), date);
        } catch (ParseException e) {
            String[] argsParts = args.split(" ");
            sessionName = argsParts[0];
            try {
                date = SessionTimes.setDateFormat.parse(args.substring(sessionName.length()));
                SessionDatabase.addSessionTimeToSpecificSession(sessionName, date);

            } catch (ParseException e1) {
                throw new BadUserInputException("Bad input format. It should be [short name] {date}."
                                                        + " Use !dateFormat for help with the date");
            }
        }

        String message = String.format(
                "New session time for %s added %s", sessionName, SessionTimes.printDateFormat.format(date)
        );
        sendMessage(event.getChannel(), message);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.DM;
    }
}
