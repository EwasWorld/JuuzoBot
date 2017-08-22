package CommandsBox;

import Foo.AbstractCommand;
import Foo.Main;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class UnlockCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
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
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        Main.setIsLocked(false);
        channel.sendMessage("Unlocked").queue();
    }
}
