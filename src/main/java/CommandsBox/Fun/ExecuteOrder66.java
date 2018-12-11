package CommandsBox.Fun;

import CommandsBox.AbstractCommand;
import CommandsBox.HelpCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class ExecuteOrder66 extends AbstractCommand {
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
        sendMessage(event.getChannel(), "The time has come, the jedi will be destroyed");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "executeOrder66";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "execute the order";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        return "";
    }
}
