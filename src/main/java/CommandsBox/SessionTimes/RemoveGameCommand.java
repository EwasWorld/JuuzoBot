package CommandsBox.SessionTimes;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import CoreBox.SessionTimes;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



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
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        SessionTimes.removeGame(args);
        channel.sendMessage("Game removed").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
