package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class ChangeWeaponsCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        UserCharacter.changeCharacterWeapon(author.getUser().getIdLong(), args);
        channel.sendMessage("Weapon change successful, enjoy your new toy.").queue();
    }
}
