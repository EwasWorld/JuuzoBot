package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class RemoveGameCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "removeGame";
    }


    @Override
    public String getDescription() {
        return "prevents time sessions for a game from being added";
    }


    @Override
    public String getArguments() {
        return "{game}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        // TODO SessionDatabase
        SessionTimes.removeGame(args);
        sendMessage(event.getChannel(), "Game removed");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
