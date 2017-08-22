package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class AttackCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(UserCharacter.attack(author.getUser(), args)).queue();
    }
}
