package CommandsBox;

import CoreBox.AbstractCommand;
import Grog.GrogList;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class PotionCommand extends AbstractCommand {
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

        channel.sendMessage(GrogList.drinkGrog(author.getUser().getName())).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
