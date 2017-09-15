package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Bot;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



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
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final Rank rank = getRank(event.getMember());
        final HelpVisibility helpVisibility = getExecuteHelpVisibility(args);
        String help = "Working commands {required} [optional]:\n";

        for (AbstractCommand command : Bot.getCommands()) {
            if (rank.hasPermission(command.getRequiredRank()) && command.getHelpVisibility() == helpVisibility) {
                help += start + command.getCommand() + getCommandArguments(command)
                        + " - " + command.getDescription() + "\n";
            }
        }

        sendMessage(event.getChannel(), help);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    static HelpVisibility getExecuteHelpVisibility(String args) {
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


    private static String getCommandArguments(AbstractCommand command) {
        String arguments = command.getArguments();
        if (arguments.equalsIgnoreCase("")) {
            return arguments;
        }
        else {
            return " " + arguments;
        }
    }
}
