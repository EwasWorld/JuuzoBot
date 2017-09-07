package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class ComplaintsCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "complaints";
    }


    @Override
    public String getDescription() {
        return "submit a complaint about the bot or its creator";
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

        channel.sendMessage(
                "You may kindly take your complaints and insert them into your anal cavity "
                        + "making sure to use plenty of lube."
        ).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
