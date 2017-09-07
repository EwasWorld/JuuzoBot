package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class FancifyCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "fancify";
    }


    @Override
    public String getDescription() {
        return "make Juuzo super fancy";
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

        channel.sendMessage("But... but... I'm already fancy af").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
