package CommandsBox.CharacterCommands;

import CharacterBox.BroadInfo.Class_;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



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
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), "Available classes: " + Class_.getClassesList());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
