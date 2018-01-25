package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class GamesListCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "gamesList";
    }


    @Override
    public String getDescription() {
        return "get a list of all games";
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

        String gamesList = SessionDatabase.getGamesList();

        if (gamesList.equals("")) {
            gamesList = "No games currently in the database";
        }

        sendMessage(event.getChannel(), gamesList);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
