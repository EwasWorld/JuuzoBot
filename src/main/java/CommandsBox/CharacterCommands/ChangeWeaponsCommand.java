package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class ChangeWeaponsCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "changeWeapon";
    }


    @Override
    public String getDescription() {
        return "change your character's weapon";
    }


    @Override
    public String getArguments() {
        return "{weapon}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        UserCharacter.changeCharacterWeapon(event.getAuthor().getIdLong(), args);
        sendMessage(event.getChannel(), "Weapon change successful, enjoy your new toy.");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
