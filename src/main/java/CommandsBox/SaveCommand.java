package CommandsBox;

import DataPersistenceBox.DataPersistence;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class SaveCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
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
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        DataPersistence.saveData();
        channel.sendMessage("Saved").queue();
    }
}
