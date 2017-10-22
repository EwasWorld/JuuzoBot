package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Bot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class UnlockCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "unlock";
    }


    @Override
    public String getDescription() {
        return "unlock the bot";
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

        Bot.setIsLocked(false);
        sendMessage(event.getChannel(), "Unlocked");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
