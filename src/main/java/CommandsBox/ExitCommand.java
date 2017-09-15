package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.DataPersistence;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ExitCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "exit";
    }


    @Override
    public String getDescription() {
        return "Save and exit the box";
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

        DataPersistence.saveData();
        sendMessage(event.getChannel(), "Bye bye :c");
        System.exit(0);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
