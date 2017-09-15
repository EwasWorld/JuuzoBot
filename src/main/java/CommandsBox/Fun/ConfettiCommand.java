package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ConfettiCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "confetti";
    }


    @Override
    public String getDescription() {
        return "celebrate and have a party";
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

        sendMessage(
                event.getChannel(),
                " :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada: "
                        + " :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada:  :tada: "
        );
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
