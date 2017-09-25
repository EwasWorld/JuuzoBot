package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.GrogList;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class PotionCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "potion";
    }


    @Override
    public String getDescription() {
        return "drink an Essence of Balthazar potion";
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

        sendMessage(event.getChannel(), GrogList.drinkGrog(event.getAuthor().getName()));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
