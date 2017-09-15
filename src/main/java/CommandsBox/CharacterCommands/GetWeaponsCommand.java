package CommandsBox.CharacterCommands;

import CharacterBox.AttackBox.Weapon;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class GetWeaponsCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "weapons";
    }


    @Override
    public String getDescription() {
        return "list of possible weapons";
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

        sendMessage(event.getChannel(), Weapon.getWeaponsList());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
