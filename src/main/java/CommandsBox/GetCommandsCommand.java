package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.Bot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import static CommandsBox.HelpCommand.getExecuteHelpVisibility;



public class GetCommandsCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "commands";
    }


    @Override
    public String getDescription() {
        return "List possible commands without descriptions";
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
        final HelpCommand.HelpVisibility helpVisibility = getExecuteHelpVisibility(args);
        StringBuilder commandString = new StringBuilder("Possible commands: *(for more detail use !help)* \n");
        for (AbstractCommand command : Bot.getCommands()) {
            if (rank.hasPermission(command.getRequiredRank()) && command.getHelpVisibility() == helpVisibility) {
                commandString.append(command.getCommand()).append(", ");
            }
        }

        commandString.delete(commandString.lastIndexOf(","), commandString.lastIndexOf(",") + 1);
        sendMessage(event.getChannel(), commandString.toString());
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }
}
