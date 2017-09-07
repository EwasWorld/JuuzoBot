package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class AttackCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "attack";
    }


    @Override
    public String getDescription() {
        return "have your character (must be created) attack your chosen victim >:]";
    }


    @Override
    public String getArguments() {
        return "{victim}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        channel.sendMessage(UserCharacter.attack(author.getUser(), args)).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
