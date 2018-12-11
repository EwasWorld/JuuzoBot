package CommandsBox.Fun;

import CommandsBox.AbstractCommand;
import CommandsBox.HelpCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class ShutUpCommand extends AbstractCommand {
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
        sendMessage(event.getChannel(), "Sheddep Mesvas, you don't know shit");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "shutUpFiktio";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "for when Fiktio does dumb shit";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        return "";
    }
}
