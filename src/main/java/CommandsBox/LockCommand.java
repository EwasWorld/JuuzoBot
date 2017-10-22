package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Bot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class LockCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "lock";
    }


    @Override
    public String getDescription() {
        return "lock the bot to prevent commands from going through";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.ADMIN;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        Bot.setIsLocked(true);
        sendMessage(event.getChannel(), "Locked");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
