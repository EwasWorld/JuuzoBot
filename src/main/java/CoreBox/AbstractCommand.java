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


        public boolean hasPermission(Rank rank) {
            return level >= rank.level;
        }
    }


    /*
     * The string which invokes the command
     */
    public abstract String getCommand();


    public abstract String getDescription();


    /*
     * Documents the possible arguments which can come after the command string (separated by spaces)
     * {required arguments} [optional arguments]
     *      No arguments if this returns ""
     */
    public abstract String getArguments();


    /*
     * The category the command falls under for when !help or similar commands are called
     */
    public abstract HelpCommand.HelpVisibility getHelpVisibility();


    /*
     * Things to do when the command is invoked
     */
    public abstract void execute(String args, MessageReceivedEvent event);


    /*
     * Check whether the member has permission to use the command
     */
    protected void checkPermission(Member member) {
        if (!getRank(member).hasPermission(getRequiredRank())) {
            throw new IncorrectPermissionsException();
        }
        else if (Bot.isIsLocked() && !member.getUser().getId().equalsIgnoreCase(IDs.eywaID)) {
            throw new BadStateException("Bot is currently locked, please try again later");
        }
    }


    /*
     * Returns the rank with the name of the corresponding highest recognised discord role
     * TODO Improve store these in a database rather than relying on roles? Allocate them using commands
     */
    protected Rank getRank(Member member) {
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


    /*
     * Returns the rank that is needed to use the command
     */
    public abstract Rank getRequiredRank();


    /*
     * Sends the given message in the given channel
     */
    protected void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }
}
