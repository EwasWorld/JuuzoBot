package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



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
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), "You look hungry :bread: ");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
