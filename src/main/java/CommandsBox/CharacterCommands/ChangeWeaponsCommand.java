package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



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
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        UserCharacter.changeCharacterWeapon(author.getUser().getIdLong(), args);
        channel.sendMessage("Weapon change successful, enjoy your new toy.").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
