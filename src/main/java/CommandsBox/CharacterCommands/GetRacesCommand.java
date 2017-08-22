package CommandsBox.CharacterCommands;

import CharacterBox.BroardInfo.Race;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class GetRacesCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "races";
    }


    @Override
    public String getDescription() {
        return "list of possible races";
    }

    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        channel.sendMessage(Race.getRacesList()).queue();
    }
}
