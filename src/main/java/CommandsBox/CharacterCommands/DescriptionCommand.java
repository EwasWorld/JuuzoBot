package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacters;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class DescriptionCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "description";
    }


    @Override
    public String getDescription() {
        return "shows the details of your current character";
    }

    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        channel.sendMessage(
                UserCharacters.getCharacterDescription(author.getUser().getIdLong())
        ).queue();
    }
}
