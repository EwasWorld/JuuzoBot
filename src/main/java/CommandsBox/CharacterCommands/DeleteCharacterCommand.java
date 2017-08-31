package CommandsBox.CharacterCommands;

import CharacterBox.UserCharacter;
import CommandsBox.HelpCommand;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class DeleteCharacterCommand extends AbstractCommand {
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
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.CHARACTER;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        UserCharacter.deleteCharacter(author.getUser().getIdLong());
        channel.sendMessage("Character deleted").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
