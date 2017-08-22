package CommandsBox;

import DataPersistenceBox.DataPersistence;
import Foo.AbstractCommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class ExitCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
        return Rank.ADMIN;
    }


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
        return "none";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        DataPersistence.saveData();
        channel.sendMessage("Bye bye :c").queue();
        System.exit(0);
    }
}
