package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class ShutUpCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "shutUpFiktio";
    }


    @Override
    public String getDescription() {
        return "for when Fiktio does dumb shit";
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

        channel.sendMessage("Sheddep Mesvas, you don't know shit").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
