package CommandsBox.CharacterCommands;

import CharacterBox.BroadInfo.Background;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GetBackgroundsCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "backgrounds";
    }


    @Override
    public String getDescription() {
        return "lists all possible backgrounds";
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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(Background.getBackgroundsList()).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
