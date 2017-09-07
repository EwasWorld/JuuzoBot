package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



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
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage(
                SessionTimes.getNextSessionAsString(author)
        ).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
