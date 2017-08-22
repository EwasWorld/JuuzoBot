package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class CharacterDescriptionCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
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
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(
                UserCharacter.getCharacterDescription(author.getUser().getIdLong())
        ).queue();
    }
}
