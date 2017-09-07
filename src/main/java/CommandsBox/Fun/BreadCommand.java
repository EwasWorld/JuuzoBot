package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class BreadCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "bread";
    }


    @Override
    public String getDescription() {
        return "need some bread";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NONE;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage("You look hungry :bread: ").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
