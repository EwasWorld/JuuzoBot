package CoreBox;

import CommandsBox.HelpCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.IncorrectPermissionsException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.Set;



/**
 * refactored 12/11/18
 */
public abstract class AbstractCommand {
    public enum Rank {
        CREATOR(4), ADMIN(3), DM(2), USER(1), BANNED(0);
        private final int level;


        Rank(int level) {
            this.level = level;
        }


        /**
         * @param rank the permission level that must be exceeded
         * @return true if this rank has permission equal to or above the given level
         */
        public boolean hasPermission(Rank rank) {
            return level >= rank.level;
        }
    }


    /**
     * @return The string which invokes the command in the chat
     */
    public abstract String getCommand();


    public abstract String getDescription();


    /**
     * Check whether the member has permission to use the command
     * @throws IncorrectPermissionsException if they don't have permission
     * @throws BadStateException if the bot is locked
     */
    protected void checkPermission(Member member) {
        checkPermission(member, getRequiredRank());
    }


    /**
     * @return The category the command falls under for when !help or similar commands are called
     */
    public abstract HelpCommand.HelpVisibility getHelpVisibility();


    /**
     * Method to call when the command is invoked
     */
    public abstract void execute(String args, MessageReceivedEvent event);


    /**
     * Check whether the member has the permission of the given rank
     *
     * @throws IncorrectPermissionsException if they don't have permission
     * @throws BadStateException if the bot is locked
     */
    protected static void checkPermission(Member member, Rank rank) {
        if (!getRank(member).hasPermission(rank)) {
            throw new IncorrectPermissionsException();
        }
        else if (Bot.isIsLocked() && !member.getUser().getId().equalsIgnoreCase(IDs.eywaID)) {
            throw new BadStateException("Bot is currently locked, please try again later");
        }
    }


    /**
     * If a valid secondary argument is present, execute it
     *
     * @param clazz The class of the enum that the secondary argument will be valid within
     * @param args in the form "<secondary args>" or "<secondary args> <other args>"
     * @param <T> the enum that the secondary argument belongs to
     * @throws BadUserInputException If argument is invalid
     */
    protected <T extends Enum<T> & SecondaryCommandAction> void executeSecondaryArgument(Class<T> clazz, String args,
                                                                                         MessageReceivedEvent event) {
        String[] split = SecondaryCommandAction.splitOutSecondaryArgument(args);
        T command;
        try {
            command = T.valueOf(clazz, split[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadUserInputException("I don't understand that argument. Use one of " + getArguments());
        }
        command.execute(split[1], event);
    }


    /**
     * TODO Improve store these in a database rather than relying on roles?
     * TODO Allocate them using commands
     * @return the highest rank that matches the user's discord roles
     */
    static protected Rank getRank(Member member) {
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
     * @return the rank that is needed to use this command
     */
    public abstract Rank getRequiredRank();


    /**
     * Sends the given message in the given channel
     */
    protected static void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }


    /**
     * What to do with a given emoji when it's added or removed from the game message
     */
    protected interface EmojiReactionAction {
        /**
         * The action to be taken when this emoji is added to the game message
         *
         * @param player the user who added the emoji
         */
        void addAction(Member player);


        /**
         * The action to be taken when this emoji is removed from the game message
         * WARNING: if this is used, it must check that removeReaction is set to false
         * Else when the reaction is cleared, this behaviour will trigger
         *
         * @param player the user who removed the emoji
         */
        void removeAction(Member player);
    }


    /**
     * @return Arguments which can come after the command name. Separated by / in the form secondary command {
     * required} [optional]. "" for no arguments. e.g. {new/end} or add {number} / get [number]
     */
    public abstract String getArguments();


    protected interface SecondaryCommandAction {
        /**
         * @param args a string in the form "<secondary args>" or "<secondary args> <other args>"
         * @return [secondary args, other args]
         */
        static String[] splitOutSecondaryArgument(String args) {
            String secondaryArgument = args.split(" ")[0];
            if (secondaryArgument.length() == args.length()) {
                args = "";
            }
            else {
                args = args.substring(secondaryArgument.length() + 1);
            }
            return new String[]{secondaryArgument, args};
        }


        /**
         * Action to be taken when the given secondary command is used
         *
         * @param args args from the message not including the primary or secondary command strings
         */
        void execute(String args, MessageReceivedEvent event);
    }
}
