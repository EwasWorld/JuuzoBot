package CommandsBox;

import CharacterBox.BroadInfo.Trinkets;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class TrinketCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "trinket";
    }


    @Override
    public String getDescription() {
        return "look around on the floor for some mildly interesting but usually useless item";
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

        channel.sendMessage(Trinkets.getTrinket(author.getUser().getName())).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
