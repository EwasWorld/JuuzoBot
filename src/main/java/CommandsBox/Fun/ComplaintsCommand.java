package CommandsBox.Fun;

import CommandsBox.AbstractCommand;
import CommandsBox.HelpCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class ComplaintsCommand extends AbstractCommand {
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
        sendMessage(
                event.getChannel(),
                "You may kindly take your complaints and insert them into your anal cavity making sure to use plenty "
                        + "of lube.");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "complaints";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "submit a complaint about the bot or its creator";
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
