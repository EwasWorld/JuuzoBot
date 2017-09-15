package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class JuicyCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "juicy";
    }


    @Override
    public String getDescription() {
        return "makes things extra juicy";
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

        sendMessage(event.getChannel(), "How about a smoothie?  :tangerine:  :tangerine: ");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
