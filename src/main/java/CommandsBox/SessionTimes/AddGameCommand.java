package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



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
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        SessionTimes.addGame(args);
        channel.sendMessage("Game added").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
