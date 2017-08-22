package CommandsBox;

import DataPersistenceBox.DataPersistence;
import CoreBox.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class SaveCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.ADMIN;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        DataPersistence.saveData();
        channel.sendMessage("Saved").queue();
    }
}
