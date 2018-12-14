package CommandsBox.Fun;

import CommandsBox.AbstractCommand;
import CommandsBox.HelpCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class FancifyCommand extends AbstractCommand {
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
        sendMessage(event.getChannel(), "But... but... I'm already fancy af");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "fancify";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "make Juuzo super fancy";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
