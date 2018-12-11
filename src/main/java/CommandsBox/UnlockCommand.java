package CommandsBox;

import CoreBox.Bot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class UnlockCommand extends AbstractCommand {
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
        Bot.setIsLocked(false);
        sendMessage(event.getChannel(), "Unlocked");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "unlock";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "unlock the bot";
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
