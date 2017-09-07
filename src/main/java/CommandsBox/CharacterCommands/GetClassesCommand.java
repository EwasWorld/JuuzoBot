package CommandsBox.CharacterCommands;

import CharacterBox.BroadInfo.Class_;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class GetClassesCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "classes";
    }


    @Override
    public String getDescription() {
        return "list of possible classes";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage("Available classes: " + Class_.getClassesList()).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
