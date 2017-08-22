package CommandsBox.CharacterCommands;

import CharacterBox.AttackBox.Weapon;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GetWeaponsCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


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
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

       channel.sendMessage(Weapon.getWeaponsList()).queue();
    }
}
