package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class TestNewChararacter extends AbstractCommand {
    @Override
    public String getCommand() {
        return "testNewChar";
    }


    @Override
    public String getDescription() {
        return "create a new character without saving it or overwriting your current character";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage(
                "Character successfully created:-\n\n"
                        + UserCharacter.createUserCharacter(args).getDescription()
        ).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
