package CommandsBox;

import CoreBox.Bot;
import CoreBox.IDs;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.IncorrectPermissionsException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;



/**
 * refactored 12/11/18
 */
public abstract class AbstractCommand implements CommandInterface {
    /**
     * Sends the given message in the given channel
     */
    protected static void sendMessage(@NotNull MessageChannel channel, @NotNull String message) {
        channel.sendMessage(message).queue();
    }


    /**
     * @return secondary arguments or an empty list if there are none
     */
    CommandInterface[] getSecondaryCommands() {
        return new CommandInterface[0];
    }


    /**
     * Check whether the member has permission to use the command
     *
     * @throws IncorrectPermissionsException if they don't have permission
     * @throws BadStateException if the bot is locked
     */
    protected void checkPermission(@NotNull Member member) {
        checkPermission(member, getRequiredRank());
    }


    /**
     * Check whether the member has the permission of the given rank
     *
     * @throws IncorrectPermissionsException if they don't have permission
     * @throws BadStateException if the bot is locked
     */
    protected static void checkPermission(@NotNull Member member, @NotNull Rank rank) {
        if (!getRank(member).hasPermission(rank)) {
            throw new IncorrectPermissionsException();
        }
        else if (Bot.isIsLocked() && !member.getUser().getId().equalsIgnoreCase(IDs.eywaID)) {
            throw new BadStateException("Bot is currently locked, please try again later");
        }
    }


    /**
     * TODO Optimisation store these in a database rather than relying on roles?
     * TODO Idea - Allocate them using commands
     *
     * @return the highest rank that matches the user's discord roles
     */
    static Rank getRank(@NotNull Member member) {
        final Set<Rank> ranks = new HashSet<>();
        for (Role role : member.getRoles()) {
            try {
                ranks.add(Rank.valueOf(role.getName().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Not a rank role, ignore it
            }
        }

        if (member.getUser().getId().equals(IDs.eywaID)) {
            return Rank.CREATOR;
        }
        else if (ranks.contains(Rank.ADMIN)) {
            return Rank.ADMIN;
        }
        else if (ranks.contains(Rank.DM)) {
            return Rank.DM;
        }
        else if (ranks.contains(Rank.BANNED)) {
            return Rank.BANNED;
        }
        else {
            return Rank.USER;
        }
    }


    /**
     * @return The category the command falls under for when !help or similar commands are called
     */
    public abstract HelpCommand.HelpVisibility getHelpVisibility();


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void execute(@NotNull String args, @NotNull MessageReceivedEvent event);


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getCommand();


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getDescription();


    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Rank getRequiredRank();


    /**
     * {@inheritDoc}
     */
    @Override
    public String getArguments() {
        if (getSecondaryCommands().length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (CommandInterface argument : getSecondaryCommands()) {
            sb.append(argument.getCommand());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    /**
     * If a valid secondary argument is present, execute it
     *
     * @param clazz The class of the enum that the secondary argument will be valid within
     * @param args in the form "<secondary args>" or "<secondary args> <other args>"
     * @param <T> the enum that the secondary argument belongs to
     * @throws BadUserInputException If argument is invalid
     */
    <T extends Enum<T> & CommandInterface> void executeSecondaryArgument(
            @NotNull Class<T> clazz, int maxWords, @NotNull String args, @NotNull MessageReceivedEvent event) {
        final String[] splitParts = args.toUpperCase().split(" ");

        // Try to find the command by each time using one extra word
        int testSize = 1;
        String testWord = "";
        T command;
        while (true) {
            try {
                if (testSize > 1) {
                    testWord += "_";
                }
                // Add on the next part to test
                testWord += splitParts[testSize - 1];
                command = T.valueOf(clazz, testWord);
                break;
            }
            // Ignore as the command may be more than the tested number of words long
            catch (IllegalArgumentException ignore) {
            }

            if (++testSize > maxWords || testSize > splitParts.length) {
                throw new BadUserInputException("I don't understand that argument. Use one of " + getArguments());
            }
        }

        checkPermission(event.getMember(), command.getRequiredRank());
        // Remove the command from the front of the args string
        int commandStrLen = command.toString().length() + testSize - 1;
        if (commandStrLen < args.length()) {
            args = args.substring(commandStrLen);
        }
        else {
            args = "";
        }
        command.execute(args, event);
    }


    /**
     * What to do with a given emoji when it's added or removed from the game message
     */
    interface EmojiReactionAction {
        /**
         * The action to be taken when this emoji is added to the game message
         *
         * @param player the user who added the emoji
         */
        void addAction(@NotNull Member player);


        /**
         * The action to be taken when this emoji is removed from the game message
         * WARNING: if this is used, it must check that removeReaction is set to false
         * Else when the reaction is cleared, this behaviour will trigger
         *
         * @param player the user who removed the emoji
         */
        void removeAction(@NotNull Member player);
    }


    protected enum Rank {
        CREATOR(4), ADMIN(3), DM(2), USER(1), BANNED(0);
        private final int level;


        Rank(int level) {
            this.level = level;
        }


        /**
         * @param rank the permission level that must be exceeded
         * @return true if this rank has permission equal to or above the given level
         */
        public boolean hasPermission(@NotNull Rank rank) {
            return level >= rank.level;
        }
    }
}
