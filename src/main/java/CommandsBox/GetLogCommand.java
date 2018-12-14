package CommandsBox;

import CoreBox.Logger;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class GetLogCommand extends AbstractCommand {
    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.ADMIN;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final Message message = new MessageBuilder().append("Log").build();
        event.getChannel().sendFile(Logger.getLoggedEventsToSend(), message).queue();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "getLog";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "returns a file of the log";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }
}
