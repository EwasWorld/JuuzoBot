package CommandsBox;

import CoreBox.Bot;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * refactored 10/12/18
 */
public class HelpCommand extends AbstractCommand {
    public enum HelpVisibility {NONE, DND, SESSION, NORMAL, ADMIN}



    private static final String mainCommandStart = ":carrot:";
    private static final String secondaryCommandStart = ":squid:";


    /**
     * {@inheritDoc}
     */
    @Override
    CommandInterface[] getSecondaryCommands() {
        return new CommandInterface[0];
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
        final Rank rank = getRank(event.getMember());
        final HelpVisibility helpVisibility = getExecuteHelpVisibility(args);

        final StringBuilder help = new StringBuilder(String.format(
                "__**Working Commands**__ %s {required} [optional], %s subcommand e.g. `!blackjack new`\n",
                mainCommandStart, secondaryCommandStart));
        for (AbstractCommand command : Bot.getCommands()) {
            if (rank.hasPermission(command.getRequiredRank()) && command.getHelpVisibility() == helpVisibility) {
                final CommandInterface[] commandInterfaces = command.getSecondaryCommands();
                help.append(String.format("\t%s %s", mainCommandStart,
                                          getCommandHelpLine(command, true, commandInterfaces.length > 0)));
                for (CommandInterface secondaryCommand : commandInterfaces) {
                    if (rank.hasPermission(secondaryCommand.getRequiredRank())) {
                        help.append(String.format("\t\t%s %s", secondaryCommandStart,
                                                  getCommandHelpLine(secondaryCommand, false, false)));
                    }
                }
            }
        }
        sendMessage(event.getChannel(), help.toString());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "help";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "lists commands with descriptions and arguments";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        return getHelpArguments();
    }


    static HelpVisibility getExecuteHelpVisibility(String args) {
        if (args.equalsIgnoreCase("")) {
            return HelpVisibility.NORMAL;
        }
        else {
            try {
                return HelpVisibility.valueOf(args.replaceAll(" ", "_").toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadUserInputException(
                        "Incorrect arguments used, try using none or one from " + getHelpArguments());
            }
        }
    }


    private String getCommandHelpLine(@NotNull CommandInterface command, boolean commandChar, boolean hasSubCommands) {
        String arguments = command.getArguments();
        if (hasSubCommands) {
            arguments = "";
        }
        else if (!arguments.equalsIgnoreCase("")) {
            arguments = " " + arguments;
        }
        final String commandCharString;
        if (commandChar) {
            commandCharString = "!";
        }
        else {
            commandCharString = "";
        }

        return (String.format(
                "`%s%s%s` - %s\n", commandCharString, command.getCommand(), arguments, command.getDescription()));
    }


    private static String getHelpArguments() {
        final StringBuilder sb = new StringBuilder("[");
        for (HelpVisibility helpVisibility : HelpVisibility.values()) {
            if (helpVisibility != HelpVisibility.NONE && helpVisibility != HelpVisibility.NORMAL) {
                sb.append(helpVisibility.toString().toLowerCase().replaceAll("_", " "));
                sb.append("/");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}
