package CommandsBox;

import Foo.AbstractCommand;
import Grog.GrogList;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class PotionCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "potion";
    }


    @Override
    public String getDescription() {
        return "drink an Essence of Balthazar potion";
    }


    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(GrogList.drinkGrog(author.getUser().getName())).queue();
    }
}
