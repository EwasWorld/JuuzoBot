package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



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
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), "Sheddep Mesvas, you don't know shit");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
