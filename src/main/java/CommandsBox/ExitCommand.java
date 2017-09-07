package CommandsBox;

import CoreBox.AbstractCommand;
import DataPersistenceBox.DataPersistence;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class ExitCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "exit";
    }


    @Override
    public String getDescription() {
        return "Save and exit the box";
    }


    @Override
    public String getArguments() {
        return "";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);

        DataPersistence.saveData();
        channel.sendMessage("Bye bye :c").queue();
        System.exit(0);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
