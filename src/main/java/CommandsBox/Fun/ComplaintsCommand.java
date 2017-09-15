package CommandsBox.Fun;

import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ComplaintsCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "complaints";
    }


    @Override
    public String getDescription() {
        return "submit a complaint about the bot or its creator";
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
                "You may kindly take your complaints and insert them into your anal cavity "
                        + "making sure to use plenty of lube."
        );
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
