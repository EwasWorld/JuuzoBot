package CoreBox;

import CommandsBox.HelpCommand;
import ExceptionsBox.BadStateException;
import ExceptionsBox.IncorrectPermissionsException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.Set;



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
     * @return Arguments which can come after the command name. Separated by spaces {required} [optional]. "" for no arguments
     */
    public abstract String getArguments();


    /**
     * @return The category the command falls under for when !help or similar commands are called
     */
    public abstract HelpCommand.HelpVisibility getHelpVisibility();


    /**
     * Method to call when the command is invoked
     */
    public abstract void execute(String args, MessageReceivedEvent event);


    /**
     * Check whether the member has permission to use the command
     * @throws IncorrectPermissionsException if they don't have permission
     * @throws BadStateException if the bot is locked
     */
    protected void checkPermission(Member member) {
        if (!getRank(member).hasPermission(getRequiredRank())) {
            throw new IncorrectPermissionsException();
        }
        else if (Bot.isIsLocked() && !member.getUser().getId().equalsIgnoreCase(IDs.eywaID)) {
            throw new BadStateException("Bot is currently locked, please try again later");
        }
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
}
