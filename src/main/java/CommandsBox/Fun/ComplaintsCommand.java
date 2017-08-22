package CommandsBox.Fun;

import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class ComplaintsCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.USER;
    }


    @Override
    public String getCommand() {
        return "complaints";
    }


    @Override
    public String getDescription() {
        return "submit a complaint about the bot or its creator";
    }


    @Override
    public String getArguments() {
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        channel.sendMessage(
                "You may kindly take your complaints and insert them into your anal cavity "
                        + "making sure to use plenty of lube."
        ).queue();
    }
}
