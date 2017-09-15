package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class FancifyCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "fancify";
    }


    @Override
    public String getDescription() {
        return "make Juuzo super fancy";
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

        sendMessage(event.getChannel(), "But... but... I'm already fancy af");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
