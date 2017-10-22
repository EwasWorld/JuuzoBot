package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.DataPersistence;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class SaveCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "save";
    }


    @Override
    public String getDescription() {
        return "save the data in the bot";
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

        DataPersistence.saveData();
        sendMessage(event.getChannel(), "Saved");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
