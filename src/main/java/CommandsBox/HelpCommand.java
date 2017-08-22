package CommandsBox;

import ExceptionsBox.BadUserInputException;
import Foo.AbstractCommand;
import Foo.Main;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class HelpCommand extends AbstractCommand {
    private static final String start = "\t:carrot: !";


    public enum HelpVisibility {NONE, CHARACTER, NORMAL}


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);
        Rank rank = getRank(author);
        HelpVisibility helpVisibility = getExecuteHelpVisibility(args);
        String help = "Working commands {required} [optional]:\n";

        for (AbstractCommand command : Main.getCommands()) {
            if (rank.hasPermission(command.getRequiredRank()) && command.getHelpVisibility() == helpVisibility) {
                help += start + command.getCommand() + getCommandArguments(command)
                        + " - " + command.getDescription() + "\n";
            }
        }

        channel.sendMessage(help).queue();
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
