package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Logger;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.util.List;



public class GetLogCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "getLog";
    }


    @Override
    public String getDescription() {
        return "returns a file of the log";
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

        Message message = new MessageBuilder().append("").build();
        try {
            // TODO: PM this rather than dumping it in the channel
            channel.sendFile(Logger.getLoggedEventsToSend(), message).queue();
        } catch (IOException e) {
            channel.sendMessage("Bzzt bzzt broken command").queue();
            e.printStackTrace();
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
