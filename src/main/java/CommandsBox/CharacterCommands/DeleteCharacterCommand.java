package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class DeleteCharacterCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "deleteChar";
    }


    @Override
    public String getDescription() {
        return "deletes your character";
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

        UserCharacter.deleteCharacter(event.getAuthor().getIdLong());
        sendMessage(event.getChannel(), "Character deleted");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
