package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class TestNewChararacter extends AbstractCommand {
    @Override
    public String getCommand() {
        return "testNewChar";
    }


    @Override
    public String getDescription() {
        return "create a new character without saving it or overwriting your current character";
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

        sendMessage(
                event.getChannel(),
                "Character successfully created:-\n\n"
                        + UserCharacter.createUserCharacter(args).getDescription()
        );
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
