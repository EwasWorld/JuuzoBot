package CommandsBox;

import CoreBox.Logger;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class ClearLogCommand extends AbstractCommand {
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
        Logger.clearLog();
        sendMessage(event.getChannel(), "Log cleared");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "clearLog";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "clears all logged errors";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.CREATOR;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        return "";
    }
}
