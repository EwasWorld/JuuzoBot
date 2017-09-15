package CommandsBox;

import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class PingCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "ping";
    }


    @Override
    public String getDescription() {
        return "test bot is working";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), "Pong");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
