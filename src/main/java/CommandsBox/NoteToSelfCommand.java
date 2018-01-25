package CommandsBox;

import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;



// TODO Implement this
public class NoteToSelfCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "";
    }


    @Override
    public String getDescription() {
        return null;
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NONE;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
