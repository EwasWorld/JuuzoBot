package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Bot;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class LockCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "lock";
    }


    @Override
    public String getDescription() {
        return "lock the bot to prevent commands from going through";
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

        Bot.setIsLocked(true);
        channel.sendMessage("Locked").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
