package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionDatabase;
import CoreBox.SessionTimes;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;



public class AddPlayerCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "addPlayer";
    }


    @Override
    public String getDescription() {
        return "add a list of users to a campaign";
    }


    @Override
    public String getArguments() {
        return "{roleName @player}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        String[] splitArgs = args.split(" ");
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();

        if (splitArgs.length != mentionedUsers.size() + 1) {
            throw new BadUserInputException("No game name provided");
        }

        for (User player : mentionedUsers) {
            SessionDatabase.addPlayer(splitArgs[0].toUpperCase(), player.getId());
        }
        sendMessage(event.getChannel(), "Player(s) successfully added.");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.DM;
    }
}
