package CommandsBox;

import Foo.AbstractCommand;
import Foo.Main;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class LockCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.ADMIN;
    }


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
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        Main.setIsLocked(true);
        channel.sendMessage("Locked").queue();
    }
}
