package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacters;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class DeleteCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "deleteChar";
    }


    @Override
    public String getDescription() {
        return "deletes your character";
    }


    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        UserCharacters.deleteCharacter(author.getUser().getIdLong());
        channel.sendMessage("Character deleted").queue();
    }
}
