package CommandsBox;

import CoreBox.AbstractCommand;
import CoreBox.GameSession;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.List;



/**
 * created 22/11/18 (collated other session commands)
 */
public class GameSessionCommand extends AbstractCommand {
    private static final String dateFormatHelp =
            "e.g. '16:00 21/8/17 BST' **or** '16:00 21/8/17 GMT + 1' (spaces around '+' are important)";
    // TODO Remove
    private static final String cheese = "{short name} {HH:mm dd/M/yy z} " + dateFormatHelp;

    @Override
    public String getCommand() {
        return "sesh";
    }


    @Override
    public String getDescription() {
        // TODO
        return "allows time sessions for a game to be added";
    }


    @Override
    public String getArguments() {
        // TODO
        return "{role} {full name}";
    }


    @Override
    public HelpCommand.HelpVisibility getHelpVisibility() {
        return HelpCommand.HelpVisibility.GAMEINFO;
    }


    @Override
    public void execute(String args, MessageReceivedEvent event) {
        checkPermission(event.getMember());
        executeSecondaryArgument(SecondaryArg.class, args, event);
    }


    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }

    enum SecondaryArg implements SecondaryCommandAction {
        ADDGAME {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                // dm, shortname, fullname
                final List<Member> mentions = event.getMessage().getMentionedMembers();
                if (mentions.size() == 0) {
                    throw new BadUserInputException("You need to @mention the DM at the start of the message");
                }
                else if (mentions.size() > 1) {
                    throw new BadUserInputException("You can only have one DM, don't mention other people, you'll just confuse the situation");
                }

                while (args.contains("  ")) {
                    args = args.replace("  ", " ");
                }
                final String[] splitArgs = args.split(" ");
                if (splitArgs.length < 3) {
                    throw new BadUserInputException("Must provide a @dm, a short name, and a full name");
                }
                final String dmID = splitArgs[0].substring(2, splitArgs[0].length() - 1);
                if (!dmID.equals(mentions.get(0).getUser().getId())) {
                    throw new BadUserInputException("The DM must be @mentioned FIRST e.g. !session add @dm shortName fullName");
                }
                final String shortName = splitArgs[1];
                final String fullName = args.substring(splitArgs[0].length() + splitArgs[1].length() + 2);

                GameSession.addGameToDatabase(shortName, fullName, mentions.get(0).getUser().getId());
                sendMessage(event.getChannel(), "Game " + shortName + " added");
            }
        },
        ADDPLAYER {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                // shortname, players
                final List<Member> mentions = event.getMessage().getMentionedMembers();
                if (mentions.size() == 0) {
                    throw new BadUserInputException("You need to @mention the players you want to add");
                }
                final String[] splitArgs = args.split(" ");
                if (splitArgs.length < 1) {
                    throw new BadUserInputException("Must provide a short name and one or more @players");
                }
                final String shortName = splitArgs[0];
                for (Member member : mentions) {
                    // TODO use an addPlayers method so that short name is only checked once
                    GameSession.addPlayer(shortName, member.getUser().getId());
                }
                sendMessage(event.getChannel(), "Player(s) added to " + shortName);
            }
        },
        ADDTIME {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                // [shortname], time
                ZonedDateTime date;
                String shortName;
                try {
                    date = DatabaseTable.parseDateFromDatabase(args);
                    shortName = GameSession.addSessionTime(event.getAuthor().getId(), date);
                } catch (ParseException e) {
                    // Might begin with shortname
                    final String[] splitArgs = args.split(" ");
                    if (splitArgs.length < 1) {
                        throw new BadUserInputException("Must provide a short name and a game time");
                    }
                    shortName = splitArgs[0];
                    final String remainingString = args.substring(shortName.length() + 1);
                    try {
                        date = DatabaseTable.parseDateFromDatabase(remainingString);
                        GameSession.addSessionTime(event.getAuthor().getId(), shortName, date);
                    } catch (ParseException e1) {
                        throw new BadUserInputException("Can't parse date: " + remainingString);
                    }
                }
                sendMessage(event.getChannel(), "The next session for " + shortName + " is set to " + DatabaseTable.formatDateForPrint(date));
            }
        },
        GAMESLIST {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                sendMessage(event.getChannel(), GameSession.getGamesList());
            }
        },
        MYSESSIONS {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                sendMessage(event.getChannel(), GameSession.getAllSessionTimes(event.getAuthor().getId()));
            }
        },
        DELETEGAME {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                GameSession.deleteGame(args);
                sendMessage(event.getChannel(), args + " deleted");
            }
        },
        NEXTGAME {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                if (args.equals("")) {
                    throw new BadUserInputException("What game do you want the next session for?");
                }
                sendMessage(event.getChannel(), "The next game for " + args + " is "
                        + DatabaseTable.formatDateForPrint(GameSession.getNextSession(args)));
            }
        },
        REMINDER {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                final String message;
                if (args.equals("")) {
                    message = GameSession.getSessionReminder(event.getAuthor().getId(), event.getGuild());
                }
                else {
                    message = GameSession.getSessionReminder(args, event.getAuthor().getId(), event.getGuild());
                }
                sendMessage(event.getChannel(), message);
            }
        },
        DATEFORMAT {
            @Override
            public void execute(String args, MessageReceivedEvent event) {
                // TODO This no longer allows dates in different timezones to be added, custom setDate with z
                sendMessage(event.getChannel(), DatabaseTable.getSetDateFormatStr());
            }
        }
    }
}
