package CommandsBox;

import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class PingCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "ping";
    }


    @Override
    public String getDescription() {
        return "test bot is working";
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

        channel.sendMessage("Pong").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
