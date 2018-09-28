package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class AddGameCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "addGame";
    }


    @Override
    public String getDescription() {
        return "allows time sessions for a game to be added";
    }


    @Override
    public String getArguments() {
        return "{role} {full name}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());
        final String[] argsParts = args.split(" ");
        if (argsParts.length < 3) {
            throw new BadUserInputException("Must provide arguments for a short name, long name, and a dm");
        }
        final int charactersInMention = argsParts[argsParts.length - 1].length();


        final String dmID = event.getMessage().getMentionedUsers().get(0).getId();
        final String shortName = argsParts[0];
        final String fullName = args.substring(shortName.length() + 1, args.length() - charactersInMention - 1);

        SessionDatabase.addGameToDatabase(shortName, fullName, dmID);
        sendMessage(event.getChannel(), "Game added");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
