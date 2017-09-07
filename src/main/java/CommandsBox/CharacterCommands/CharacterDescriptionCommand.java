package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class CharacterDescriptionCommand extends AbstractCommand {
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
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage(
                UserCharacter.getCharacterDescription(author.getUser().getIdLong())
        ).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
