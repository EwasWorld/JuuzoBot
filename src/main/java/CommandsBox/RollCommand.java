package CommandsBox;

import CharacterBox.UserCharacter;
import CoreBox.AbstractCommand;
import CoreBox.Roll;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;



public class RollCommand extends AbstractCommand {
    @Override
    public String getCommand() {
        return "roll";
    }


    @Override
    public String getDescription() {
        return "roll a die or skill check, saving throw, or initiative";
    }


    @Override
    public String getArguments() {
        return "[quantity] d {die size} [modifier] **OR** {skill/ability/initiative}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());

        final User user = event.getAuthor();
        final String result;
        if (!args.equals("")) {
            if (containsDigit(args)) {
                result = Roll.rollDieFromChatEvent(args, user.getName());
            }
            else {
                result = UserCharacter.roll(user.getIdLong(), args);
            }
            sendMessage(event.getChannel(), result);
        }
        else {
            throw new IllegalArgumentException("Arguments missing. See !help for details");
        }
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    private static boolean containsDigit(String message) {
        for (int i = 0; i < 3; i++) {
            if (Character.isDigit(message.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
