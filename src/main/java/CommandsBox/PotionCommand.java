package CommandsBox;

import CoreBox.GrogList;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class PotionCommand extends AbstractCommand {
    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
        sendMessage(event.getChannel(), GrogList.drinkGrog(event.getAuthor().getName()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "potion";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "drink an Essence of Balthazar potion";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
