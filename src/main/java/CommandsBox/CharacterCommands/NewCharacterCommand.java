package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class NewCharacterCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "newChar";
    }


    @Override
    public String getDescription() {
        return "create a character";
    }


    @Override
    public String getArguments() {
        return "{name} [subrace] {race} {class} {background}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final long userID = event.getAuthor().getIdLong();
        UserCharacter.createUserCharacterAndAddToMap(userID, args);
        sendMessage(
                event.getChannel(),
                "Character successfully created:-\n\n" + UserCharacter.getCharacterDescription(userID)
        );
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
