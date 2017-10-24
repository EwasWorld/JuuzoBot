package CommandsBox;

import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;



public class BlackJackEndGameCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "blackjackEnd";
    }


    @Override
    public String getDescription() {
        return "Ends the current game of blackjack";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.ADMIN;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        BlackJackCommand.setGameRunningFalse();
        sendMessage(event.getTextChannel(), "Game ended");
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
