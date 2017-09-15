package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class BreakCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "break";
    }


    @Override
    public String getDescription() {
        return "break the bot";
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

        sendMessage(event.getChannel(), "Bzzt bzzt **starts smoking** *distant shouts from Eywa*");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
