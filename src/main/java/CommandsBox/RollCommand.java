package CommandsBox;

import CharacterBox.UserCharacter;
import CoreBox.AbstractCommand;
import CoreBox.Roll;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class RollCommand extends AbstractCommand {
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


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
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author);

        final String result;
        if (!args.equals("")) {
            if (containsDigit(args)) {
                result = Roll.rollDieFromChatEvent(args, author.getUser().getName());
            }
            else {
                result = UserCharacter.roll(author.getUser().getIdLong(), args);
            }
            channel.sendMessage(result).queue();
        }
        else {
            throw new IllegalArgumentException("Arguments missing. See !help for details");
        }
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.NORMAL;
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
