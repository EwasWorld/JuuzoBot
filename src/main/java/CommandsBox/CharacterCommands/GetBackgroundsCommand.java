package CommandsBox.CharacterCommands;

import CharacterBox.BroadInfo.Background;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



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
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), Background.getBackgroundsList());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
