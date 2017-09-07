package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Bot;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;



public class HelpCommand extends AbstractCommand {
    public enum HelpVisibility {NONE, CHARACTER, NORMAL}
    private static final String start = "\t:carrot: !";


    @Override
    public String getCommand() {
        return "help";
    }


    @Override
    public String getDescription() {
        return "lists commands with descriptions and arguments";
    }


    @Override
    public String getArguments() {
        return "[char]";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author,
                        List<User> mentions) {
        checkPermission(author);
        Rank rank = getRank(author);
        HelpVisibility helpVisibility = getExecuteHelpVisibility(args);
        String help = "Working commands {required} [optional]:\n";

        for (AbstractCommand command : Bot.getCommands()) {
            if (rank.hasPermission(command.getRequiredRank()) && command.getHelpVisibility() == helpVisibility) {
                help += start + command.getCommand() + getCommandArguments(command)
                        + " - " + command.getDescription() + "\n";
            }
        }

        channel.sendMessage(help).queue();
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    private HelpVisibility getExecuteHelpVisibility(String args) {
        if (args.equalsIgnoreCase("")) {
            return HelpVisibility.NORMAL;
        }
        else if (args.equalsIgnoreCase("char")) {
            return HelpVisibility.CHARACTER;
        }
        else {
            throw new BadUserInputException("Incorrect arguments for !help, try using none");
        }
    }


    private String getCommandArguments(AbstractCommand command) {
        String arguments = command.getArguments();
        if (arguments.equalsIgnoreCase("")) {
            return arguments;
        }
        else {
            return " " + arguments;
        }
    }
}
