package CommandsBox;

import CharacterBox.BroadInfo.Trinkets;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class TrinketCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "trinket";
    }


    @Override
    public String getDescription() {
        return "look around on the floor for some mildly interesting but usually useless item";
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

        sendMessage(event.getChannel(), Trinkets.getTrinket(event.getAuthor().getName()));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
