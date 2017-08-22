package CommandsBox;

import Foo.AbstractCommand;
import Foo.Main;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class UnlockCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.ADMIN;
    }


    @Override
    public String getCommand() {
        return "unlock";
    }


    @Override
    public String getDescription() {
        return "unlock the bot";
    }


    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        Main.setIsLocked(false);
        channel.sendMessage("Unlocked").queue();
    }
}
