package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class NewCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


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
        return "{name} [subrace] {race} {class}";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        UserCharacter.createUserCharacter(author.getUser().getIdLong(), args);
        channel.sendMessage(
                "Character successfully created\n"
                        + UserCharacter.getCharacterDescription(author.getUser().getIdLong())
        ).queue();

    }
}
