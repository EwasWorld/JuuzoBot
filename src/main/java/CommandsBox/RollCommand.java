package CommandsBox;

import CharacterBox.UserCharacter;
import CoreBox.Die;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;



/**
 * updated style 10/12/18
 */
public class RollCommand extends AbstractCommand {
    private static final String invalidFormat = "Invalid input see !help for details";


    /**
     * {@inheritDoc}
     */
    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.DND;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
        checkPermission(event.getMember());
        args = args.replace(" ", "");
        if (args.equals("")) {
            throw new IllegalArgumentException("Arguments missing. See !help for details");
        }

        final User author = event.getAuthor();
        final String result;
        if (containsDigit(args)) {
            // args = [adv/dis] [quantity] d {die size} [modifier]
            result = String.format("%s %s", author.getName(), roll(args));
        }
        else {
            // args = name {skill/ability/initiative}
            // TODO TEST are there any second args that could be more than one word?
            String[] argsSplit = args.split(" ");
            final String other = argsSplit[argsSplit.length - 1];
            argsSplit = new String[]{args.substring(0, args.length() - other.length() - 1)};
            result = UserCharacter.roll(author.getId(), argsSplit[0], argsSplit[1]);
        }
        sendMessage(event.getChannel(), result);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "roll";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "roll a die or skill check, saving throw, or initiative";
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
        return "[adv/dis] [quantity] d {die size} [modifier] **OR** {skill/ability/initiative} "
                + "e.g !roll adv 1d20+3 **OR** !roll strength";
    }


    private static boolean containsDigit(String message) {
        for (int i = 0; i < 3; i++) {
            if (Character.isDigit(message.charAt(i))) {
                return true;
            }
        }
        return false;
    }


    /**
     * @return the roll string for a message in the format [adv/dis] [quantity] d {size} [modifier]
     */
    public static String roll(String message) {
        final Die.RollType rollType = getRollType(message);
        if (rollType != Die.RollType.NORMAL) {
            message = message.substring(3);
        }

        try {
            final String[] messageParts = splitMessageOnD(message);
            final int[] dieAndModifier = separateDieAndModifier(messageParts[1]);
            final Die die = new Die(getQuantity(messageParts[0]), dieAndModifier[0], dieAndModifier[1]);
            return die.getStringForRoll(rollType);
        } catch (IllegalArgumentException e) {
            // TODO Optimisation where is this thrown from if anywhere?
            throw new BadUserInputException(invalidFormat);
        }
    }


    /**
     * If message doesn't begin with 'adv' or 'dis' it returns normal (otherwise returns the corresponding type)
     */
    private static Die.RollType getRollType(String message) {
        if (message.startsWith("adv")) {
            return Die.RollType.ADVANTAGE;
        }
        else if (message.startsWith("dis")) {
            return Die.RollType.DISADVANTAGE;
        }
        else {
            return Die.RollType.NORMAL;
        }
    }


    /**
     * Splits a message in the format $d$ where $ does not contain 'd's
     * TODO Optimisation is there a better way to check the format of the input message?
     */
    private static String[] splitMessageOnD(String message) {
        if (!message.contains("d") || message.equals("d")) {
            throw new BadUserInputException(invalidFormat);
        }

        final String[] messageParts = message.split("d");
        if (messageParts.length != 2) {
            throw new BadUserInputException(invalidFormat);
        }

        return messageParts;
    }


    /**
     * @param message a string in the format
     * '#' dieSize
     * '#+#' dieSize and positive modifier
     * '#-#' dieSize and negative modifier
     * @return {dieSize, modifier}
     */
    private static int[] separateDieAndModifier(String message) {
        if (!hasModifier(message)) {
            return new int[]{parseInt(message), 1};
        }

        final String negative = "-";
        final String splitOn;
        if (message.contains("+")) {
            splitOn = "\\+";
        }
        else {
            splitOn = negative;
        }

        final String[] parts = message.split(splitOn);
        if (parts.length != 2) {
            throw new BadUserInputException("Incorrect die size or modifier");
        }

        // Re-add the sign to the string so that int is parsed correctly for negatives
        if (splitOn.equals(negative)) {
            parts[1] = splitOn + parts[1];
        }

        return new int[]{parseInt(parts[0]), parseInt(parts[1])};
    }


    /**
     * Parses the quantity
     *
     * @return 1 if no quantity was given
     */
    private static int getQuantity(String quantityStr) {
        if (quantityStr.length() != 0) {
            return parseInt(quantityStr);
        }
        else {
            return 1;
        }
    }


    /**
     * @param message a string of a dieSize (and modifier)
     * @return whether the modifier is present
     */
    private static boolean hasModifier(String message) {
        return message.contains("+") || message.contains("-");
    }


    private static int parseInt(String intString) {
        return Integer.parseInt(intString);
    }
}
