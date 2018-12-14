package CommandsBox;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * TODO Implement command
 * updated style 10/12/18
 */
public class NoteToSelfCommand extends AbstractCommand {
    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NONE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
