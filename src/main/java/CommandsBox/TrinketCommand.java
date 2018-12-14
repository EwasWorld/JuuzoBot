package CommandsBox;

import CharacterBox.BroadInfo.Trinkets;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class TrinketCommand extends AbstractCommand {
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

        sendMessage(event.getChannel(), Trinkets.getTrinket(event.getAuthor().getName()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "trinket";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "look around on the floor for some mildly interesting but usually useless item";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
