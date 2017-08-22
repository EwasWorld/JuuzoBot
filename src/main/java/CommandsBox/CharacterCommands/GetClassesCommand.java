package CommandsBox.CharacterCommands;

import CharacterBox.BroardInfo.Class_;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GetClassesCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "classes";
    }


    @Override
    public String getDescription() {
        return "list of possible classes";
    }

    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        channel.sendMessage(Class_.getClassesList()).queue();
    }
}
