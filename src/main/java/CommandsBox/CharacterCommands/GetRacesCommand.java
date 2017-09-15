package CommandsBox.CharacterCommands;

import CharacterBox.BroadInfo.Race;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class GetRacesCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "races";
    }


    @Override
    public String getDescription() {
        return "list of possible races";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), Race.getRacesList());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
