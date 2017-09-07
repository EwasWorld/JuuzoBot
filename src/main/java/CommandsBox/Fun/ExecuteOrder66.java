package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class ExecuteOrder66 extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "executeOrder66";
    }


    @Override
    public String getDescription() {
        return "execute the order";
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

        channel.sendMessage("The time has come, the jedi will be destroyed").queue();
    }
}
