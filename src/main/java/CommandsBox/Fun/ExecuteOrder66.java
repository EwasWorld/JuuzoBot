package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ExecuteOrder66 extends AbstractCommand {
    @Override
    public String getCommand() {
        return "executeOrder66";
    }


    @Override
    public String getDescription() {
        return "execute the order";
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

        sendMessage(event.getChannel(), "The time has come, the jedi will be destroyed");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
