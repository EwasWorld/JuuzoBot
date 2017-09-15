package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class AttackCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "attack";
    }


    @Override
    public String getDescription() {
        return "have your character (must be created) attack your chosen victim >:]";
    }


    @Override
    public String getArguments() {
        return "{victim}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        sendMessage(event.getChannel(), UserCharacter.attack(event.getAuthor(), args));
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
