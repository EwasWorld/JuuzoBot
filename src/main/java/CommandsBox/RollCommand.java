package CommandsBox;

import CharacterBox.UserCharacters;
import Foo.AbstractCommand;
import Foo.Roll;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;



public class RollCommand extends AbstractCommand {
    @Override
    public Rank getCommandCategory() {
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
        return "[quantity] d {die size} [modifier]\n{skill/ability/initiative}";
    }


    @Override
    public void execute(String args, MessageChannel channel, Member author) {
        checkPermission(author.getUser());

        final String result;
        if (!args.equals("")) {
            if (containsDigit(args)) {
                result = Roll.rollDieFromChatEvent(args, author.getUser().getName());
            }
            else {
                result = UserCharacters.roll(author.getUser().getIdLong(), args);
            }
            channel.sendMessage(result).queue();
        }
        else {
            throw new IllegalArgumentException("Arguments missing. See !help for details");
        }
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
