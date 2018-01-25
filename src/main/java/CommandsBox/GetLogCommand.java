package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Logger;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;



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
        return HelpCommand.HelpVisibility.ADMIN;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final Message message = new MessageBuilder().append("Log").build();
        try {
            // TODO Implement PM this rather than dumping it in the channel
            event.getChannel().sendFile(Logger.getLoggedEventsToSend(), message).queue();
        } catch (IOException e) {
            sendMessage(event.getChannel(), "Bzzt bzzt broken command");
            e.printStackTrace();
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
