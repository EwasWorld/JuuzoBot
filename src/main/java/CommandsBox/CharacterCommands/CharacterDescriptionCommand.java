package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class CharacterDescriptionCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "description";
    }


    @Override
    public String getDescription() {
        return "shows the details of your current character";
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

        sendMessage(event.getChannel(), UserCharacter.getCharacterDescription(event.getAuthor().getIdLong()));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
