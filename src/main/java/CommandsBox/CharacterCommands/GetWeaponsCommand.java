package CommandsBox.CharacterCommands;

import CharacterBox.AttackBox.Weapon;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



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
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage(Weapon.getWeaponsList()).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
