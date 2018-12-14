package CommandsBox;

import CoreBox.GameSession;
import DatabaseBox.DatabaseTable;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;



/**
 * created 22/11/18 (collated other session commands)
 */
public class GameSessionCommand extends AbstractCommand {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/M/yy Z");


    private static String getDateFormatHelp() {
        return "(see !session " + SecondaryArg.DATE_FORMAT.getCommand() + ")";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    CommandInterface[] getSecondaryCommands() {
        return SecondaryArg.values();
    }


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
        executeSecondaryArgument(SecondaryArg.class, 2, args, event);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return "session";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Organise sessions between players and @mention them with reminders when it's time";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rank getRequiredRank() {
        return Rank.USER;
    }


    enum SecondaryArg implements CommandInterface {
        ADD_GAME {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                final List<Member> mentions = event.getMessage().getMentionedMembers();
                if (mentions.size() == 0) {
                    throw new BadUserInputException("You need to @mention the DM at the start of the message");
                }
                else if (mentions.size() > 1) {
                    throw new BadUserInputException(
                            "You can only have one DM, don't mention other people, you'll just confuse the situation");
                }

                while (args.contains("  ")) {
                    args = args.replaceAll("  ", " ");
                }
                final String[] splitArgs = args.split(" ");
                if (splitArgs.length < 3) {
                    throw new BadUserInputException("Must provide a @dm, a short name, and a full name");
                }
                final String dmID = splitArgs[0].substring(2, splitArgs[0].length() - 1);
                if (!dmID.equals(mentions.get(0).getUser().getId())) {
                    throw new BadUserInputException(
                            "The DM must be @mentioned FIRST e.g. !session add @dm shortName fullName");
                }
                final String shortName = splitArgs[1];
                final String fullName = args.substring(splitArgs[0].length() + splitArgs[1].length() + 2);

                GameSession.addGameToDatabase(shortName, fullName, mentions.get(0).getUser().getId());
                sendMessage(event.getChannel(), "Game " + shortName + " added");
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Add a new session";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.DM;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{@host/@creator} {short name (no spaces in here)} {full name}";
            }
        },
        ADD_PLAYER {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
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


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Add a new session";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.DM;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{short name} {@player} [@player] [@player] etc.";
            }
        },
        ADD_TIME {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                String[] split = args.split(" ");
                String shortName = null;
                String dateString = args;
                if (split.length == 4) {
                    shortName = split[0];
                    dateString = args.substring(shortName.length() + 1);
                }
                else if (split.length != 3) {
                    throw new BadUserInputException("Invalid arguments, you need them in the form " + getArguments());
                }

                ZonedDateTime date;
                int gmtOffset;
                try {
                    final String[] dateStringSplit = dateString.split(" ");
                    // Should split into "time date GMTOffset"
                    if (dateStringSplit.length != 3) {
                        throw new BadUserInputException("Invalid date format, " + getDateFormatHelp());
                    }
                    gmtOffset = Integer.parseInt(dateStringSplit[2]);
                    // Get the date in the form "time date +HH00"
                    final String formattedDateString = String.format("%s %s %0+3d00", dateStringSplit[0],
                                                                     dateStringSplit[1], gmtOffset);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    date = ZonedDateTime.ofInstant(dateFormat.parse(formattedDateString).toInstant(), ZoneId.of("UTC"));
                } catch (ParseException e1) {
                    throw new BadUserInputException("Can't parse date: " + dateString + "\n" + getDateFormatHelp());
                }

                if (shortName == null) {
                    shortName = GameSession.addSessionTime(event.getAuthor().getId(), date);
                }
                else {
                    GameSession.addSessionTime(event.getAuthor().getId(), shortName, date);
                }
                sendMessage(event.getChannel(), "The next session for " + shortName + " is set to "
                        + DatabaseTable.formatDateForPrint(date, gmtOffset));
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Add a time for the next session";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.DM;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "[short name] {time " + getDateFormatHelp() + "}";
            }
        },
        GAMES_LIST {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                sendMessage(event.getChannel(), GameSession.getGamesList());
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "See the full names and short names of all the games in the system";
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
                return "";
            }
        },
        MY_SESSIONS {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                int offset = 0;
                try {
                    if (!args.equals("")) {
                        offset = Integer.parseInt(args);
                    }
                } catch (IllegalArgumentException e) {
                    throw new BadUserInputException("Cannot parse the number " + args);
                }
                sendMessage(event.getChannel(), GameSession.getAllSessionTimes(event.getAuthor().getId(), offset));
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "See all the games you're in or hosting";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.DM;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "[timezone offset from GMT (e.g. -3)]";
            }
        },
        DELETE_GAME {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                GameSession.deleteGame(args);
                sendMessage(event.getChannel(), args + " deleted");
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Deletes a game from the database";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.DM;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{short name}";
            }
        },
        NEXT_GAME {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                if (args.equals("")) {
                    throw new BadUserInputException("What game do you want the next session for?");
                }
                String[] split = args.split(" ");
                String shortName;
                int offset = 0;
                if (split.length > 2) {
                    throw new BadUserInputException("Invalid arguments, you need them in the form " + getArguments());
                }
                else if (split.length == 2) {
                    shortName = split[0];
                    try {
                        offset = Integer.parseInt(split[1]);
                    } catch (IllegalArgumentException e) {
                        throw new BadUserInputException("Cannot parse the number " + split[1]);
                    }
                }
                else {
                    shortName = args;
                }

                sendMessage(event.getChannel(), "The next game for " + shortName + " is "
                        + DatabaseTable.formatDateForPrint(GameSession.getNextSession(shortName), offset));
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Gets the next session time of the specified game";
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
                return "{short name} [timezone offset from GMT (e.g. -3)]";
            }
        },
        REMINDER {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                final String message;
                if (args.equals("")) {
                    message = GameSession.getSessionReminder(event.getAuthor().getId(), event.getGuild());
                }
                else {
                    message = GameSession.getSessionReminder(args, event.getAuthor().getId(), event.getGuild());
                }
                sendMessage(event.getChannel(), message);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Create a reminder including a countdown and an @mention to all players";
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public Rank getRequiredRank() {
                return Rank.DM;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getArguments() {
                return "{short name}";
            }
        },
        DATE_FORMAT {
            /**
             * {@inheritDoc}
             */
            @Override
            public void execute(@NotNull String args, @NotNull MessageReceivedEvent event) {
                final String dateFormatHelp = "Dates must be in the form `" + dateFormat.toPattern()
                        + "` using a 24 hour clock where `Z` is the time zone offset from GMT"
                        + " (a non-decimal number from 12 to -14) e.g. `16:00 21/8/17 +1` or `16:00 21/8/17 1` both"
                        + " meaning GMT+1 or `03:00 10/2/17 -9` meaning GMT-9";
                sendMessage(event.getChannel(), dateFormatHelp);
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public String getDescription() {
                return "Explains the date format for setting a session time";
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
                return "";
            }
        };


        /**
         * {@inheritDoc}
         */
        @Override
        public String getCommand() {
            return this.toString().toLowerCase().replaceAll("_", " ");
        }
    }
}
