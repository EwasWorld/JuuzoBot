package CommandsBox;

import CoreBox.AbstractCommand;
import DataPersistenceBox.DataPersistence;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class SaveCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "save";
    }


    @Override
    public String getDescription() {
        return "save the data in the bot";
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
        channel.sendMessage("Saved").queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }
}
