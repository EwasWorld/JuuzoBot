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


    public abstract String getCommand();


    public abstract String getDescription();


    public abstract String getArguments();


    public abstract HelpCommand.HelpVisibility getHelpVisibility();


    public abstract void execute(String args, MessageReceivedEvent event);


    protected void checkPermission(Member member) {
        if (!getRank(member).hasPermission(getRequiredRank())) {
            throw new IncorrectPermissionsException();
        }
        else if (Bot.isIsLocked() && !member.getUser().getId().equalsIgnoreCase(IDs.eywaID)) {
            throw new BadStateException("Bot is currently locked, please try again later");
        }
    }


    protected Rank getRank(Member member) {
        Set<Rank> ranks = new HashSet<>();
        for (Role role : member.getRoles()) {
            try {
                ranks.add(Rank.valueOf(role.getName().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Not a rank roll, ignore it
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


    public abstract Rank getRequiredRank();


    protected void sendMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }
}
