package CoreBox;

import DatabaseBox.*;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.ContactEwaException;
import ExceptionsBox.FeatureUnavailableException;
import net.dv8tion.jda.core.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;



/**
 * TODO Optimisation reduce the number of database calls for things like checking shortnames (try to have it only
 * happen once for any action)
 * TODO Change short name from key to row index as key
 * refactored 22/11/18
 */
public class GameSession {
    private static final DatabaseTable sessionsDatabaseTable = new DatabaseTable(
            "Sessions", SessionsDatabaseFields.values(), new PrimaryKey(Collections.singleton(
            SessionsDatabaseFields.SHORT_NAME.fieldName)));
    private static final DatabaseTable playersDatabaseTable = new DatabaseTable(
            "SessionPlayers", SessionPlayersDatabaseFields.values());
    private static final DatabaseWrapper databaseWrapper = new DatabaseWrapper(
            new DatabaseTable[]{sessionsDatabaseTable, playersDatabaseTable});


    /**
     * Helper method for testing
     */
    public static boolean checkRowCounts(int sessions, int players) {
        DatabaseWrapper.checkDatabaseInTestMode();
        return databaseWrapper.checkRowCounts(new int[]{sessions, players});
    }


    /**
     * Helper method for testing
     */
    public static DatabaseWrapper getDatabaseWrapper() {
        DatabaseWrapper.checkDatabaseInTestMode();
        return databaseWrapper;
    }


    /**
     * @param shortName Ideally a shorter version of the full name and easy to type and can be used to identify the game
     * @param fullName the full name of the game
     * @param dmID the ID of the DM
     * @throws BadUserInputException if the short name is not unique
     */
    public static void addGameToDatabase(@NotNull String shortName, @NotNull String fullName, @NotNull String dmID) {
        if (shortName.equals("")) {
            throw new BadUserInputException("Short name cannot be nothing");
        }
        else if (shortNameExists(shortName)) {
            throw new BadUserInputException(
                    "There's already a game with that short name, you'll have to choose another I'm afraid");
        }

        sessionsDatabaseTable.insert(new SetArgs(sessionsDatabaseTable, Map.of(
                SessionsDatabaseFields.FULL_NAME.fieldName, fullName, SessionsDatabaseFields.DM_ID.fieldName, dmID,
                SessionsDatabaseFields.SHORT_NAME.fieldName, shortName)));
    }


    private static boolean shortNameExists(@NotNull String shortName) {
        return (boolean) sessionsDatabaseTable.select(new Args(
                sessionsDatabaseTable, SessionsDatabaseFields.SHORT_NAME.fieldName, shortName), ResultSet::next);
    }


    /**
     * Updates the session associated with shortName with a date for the next session
     *
     * @param userID the user who wants to add a session time
     * @throws BadUserInputException if the game specified does not belong to the user
     */
    public static void addSessionTime(@NotNull String userID, @NotNull String shortName, @NotNull ZonedDateTime date) {
        if (!getDmId(shortName).equals(userID)) {
            throw new BadUserInputException("This isn't your game, only the DM can change the session time");
        }
        addSessionTimeToSpecificSession(shortName, date);
    }


    /**
     * @return the id of the dm associated with the shortname
     * @throws BadUserInputException if the game is not found
     */
    private static String getDmId(@NotNull String shortName) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        return (String) sessionsDatabaseTable.select(
                new Args(sessionsDatabaseTable, SessionsDatabaseFields.SHORT_NAME.fieldName, shortName),
                rs -> {
                    rs.next();
                    return rs.getString(SessionsDatabaseFields.DM_ID.fieldName);
                });
    }


    /**
     * Updates the next session time for a given session
     */
    private static void addSessionTimeToSpecificSession(@NotNull String shortName, @NotNull ZonedDateTime date) {
        // Bypass this check when testing
        if (date.isBefore(ZonedDateTime.now()) && !DatabaseTable.isInTestMode()) {
            throw new BadUserInputException(
                    "It's generally ill advised to organise an event to happen in the past... try a time in the "
                            + "future.");
        }
        sessionsDatabaseTable.update(
                new SetArgs(sessionsDatabaseTable, Map.of(SessionsDatabaseFields.SESSION_TIME.fieldName, date)),
                new Args(sessionsDatabaseTable, SessionsDatabaseFields.SHORT_NAME.fieldName, shortName));
    }


    /**
     * Updates the session associated with the dm with a date for the next session
     *
     * @return the short name of the session the time has been added to
     */
    public static String addSessionTime(@NotNull String dmID, @NotNull ZonedDateTime date) {
        final String shortName = getDmsOnlyGame(dmID);
        addSessionTimeToSpecificSession(shortName, date);
        return shortName;
    }


    /**
     * @return the shortName of the only game the dmID runs
     * @throws BadUserInputException if the dm does not dm exactly one game
     */
    private static String getDmsOnlyGame(@NotNull String dmID) {
        String[] dmGames = getDmGamesShortNames(dmID);
        if (dmGames.length == 0) {
            throw new BadUserInputException("You don't dm any games");
        }
        else if (dmGames.length != 1) {
            throw new BadUserInputException("You dm more than 1 game, specify which you want to update");
        }
        return dmGames[0];
    }


    /**
     * @return short names of all the games that the given dm runs
     */
    private static String[] getDmGamesShortNames(@NotNull String dmID) {
        return (String[]) sessionsDatabaseTable.select(
                new Args(sessionsDatabaseTable, SessionsDatabaseFields.DM_ID.fieldName, dmID),
                rs -> {
                    final List<String> shortNames = new ArrayList<>();
                    while (rs.next()) {
                        shortNames.add(rs.getString(SessionsDatabaseFields.SHORT_NAME.fieldName));
                    }
                    return shortNames.toArray(new String[0]);
                });
    }


    /**
     * Add a player to a game
     *
     * @throws BadUserInputException if the game is not found or if the player is already in the game
     */
    public static void addPlayer(@NotNull String shortName, @NotNull String playerID) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        final Args args = new Args(playersDatabaseTable, Map.of(
                SessionPlayersDatabaseFields.SHORT_NAME.fieldName, shortName,
                SessionPlayersDatabaseFields.PLAYER.fieldName, playerID));

        playersDatabaseTable.select(args, rs -> {
            if (rs.next()) {
                throw new BadUserInputException("This player has already been added to the game");
            }
            return null;
        });
        playersDatabaseTable.insert(new SetArgs(args));
    }


    /**
     * @return All games the player takes part in with their next session time or n/a (sorted oldest-newest
     * @throws BadUserInputException if the game is not found
     */
    public static String getAllSessionTimes(@NotNull String playerID, int gmtOffset) {
        final String[] playerGamesArray = getPlayerGamesShortNames(playerID);
        final String[] dmGamesArray = getDmGamesShortNames(playerID);
        if (playerGamesArray.length == 0 && dmGamesArray.length == 0) {
            throw new BadUserInputException("You don't seem to be added to any games (or DMing any)");
        }

        final String format = "%s%s: %s\n";
        final String dmStar = "*";
        final List<String> playerGames = new LinkedList<>(Arrays.asList(playerGamesArray));
        final List<String> dmGames = new LinkedList<>(Arrays.asList(dmGamesArray));
        final Args args = new Args(sessionsDatabaseTable);
        args.setOrderBy(SessionsDatabaseFields.SESSION_TIME.fieldName, true);
        return (String) sessionsDatabaseTable.select(args, rs -> {
            final String pastGame = "^";
            final StringBuilder sb = new StringBuilder();
            // force games with no date to the bottom of the list
            final StringBuilder sbNoDate = new StringBuilder();
            while (rs.next()) {
                final String shortName = rs.getString(SessionsDatabaseFields.SHORT_NAME.fieldName);
                boolean isDmGame = dmGames.contains(shortName);
                if (playerGames.contains(shortName) || isDmGame) {
                    String dmStarUse = "";
                    if (isDmGame) {
                        dmStarUse = dmStar;
                    }
                    final ZonedDateTime gameTime;
                    try {
                        gameTime = DatabaseTable.parseDateFromDatabase(
                                rs.getString(SessionsDatabaseFields.SESSION_TIME.fieldName));
                        if (gameTime.isBefore(ZonedDateTime.now())) {
                            if (gameTime.isBefore(ZonedDateTime.now().minusDays(1))) {
                                throw new IllegalArgumentException("Old date");
                            }
                            else {
                                dmStarUse += pastGame;
                            }
                        }
                        sb.append(String.format(format, shortName, dmStarUse,
                                                DatabaseTable.formatDateForPrint(gameTime, gmtOffset)));
                    } catch (ParseException | NullPointerException | IllegalArgumentException e) {
                        sbNoDate.append(String.format(format, shortName, dmStarUse, "n/a"));
                    }
                    playerGames.remove(shortName);
                    dmGames.remove(shortName);
                }
            }
            sb.append(sbNoDate);
            // key
            final boolean isDM = sb.indexOf(dmStar) != -1;
            final boolean isPastGame = sb.indexOf(pastGame) != -1;
            if (isDM || isPastGame) {
                sb.append("Note: ");
                if (isDM) {
                    sb.append(dmStar);
                    sb.append(" = games you DM");
                }
                if (isPastGame) {
                    if (isDM) {
                        sb.append(", ");
                    }
                    sb.append(pastGame);
                    sb.append(" = games in the past");
                }
            }
            return sb.toString();
        });
    }


    private static String[] getPlayerGamesShortNames(@NotNull String playerID) {
        return (String[]) playersDatabaseTable.select(
                new Args(playersDatabaseTable, SessionPlayersDatabaseFields.PLAYER.fieldName, playerID),
                rs -> {
                    final List<String> shortNames = new ArrayList<>();
                    while (rs.next()) {
                        shortNames.add(rs.getString(SessionPlayersDatabaseFields.SHORT_NAME.fieldName));
                    }
                    return shortNames.toArray(new String[0]);
                });
    }


    /**
     * @param userID user who made the call for the reminder must be the dm of the game
     * @return a string mentioning all players with a countdown to next session
     * @throws BadUserInputException if the game doesn't belong to the user who made the call
     */
    public static String getSessionReminder(@NotNull String shortName, @NotNull String userID, @NotNull Guild guild) {
        if (!getDmId(shortName).equals(userID)) {
            throw new BadUserInputException("This isn't your game, only the DM can send a reminder");
        }
        return getSessionReminderAux(shortName, guild);
    }


    /**
     * @return a string containing a mention to all players in the game and a countdown to the next session
     * @throws BadStateException if there is no time for the session or the time is in the past
     */
    private static String getSessionReminderAux(@NotNull String shortName, @NotNull Guild guild) {
        final ZonedDateTime date = getNextSession(shortName);
        if (date == null) {
            throw new BadStateException("There is no next session set for this game");
        }
        else if (date.isBefore(ZonedDateTime.now())) {
            throw new BadStateException(
                    "The next session is in the past, it's pointless reminding people of what's already happened, how"
                            + " about updating the game time first");
        }

        final String[] players = getPlayersInGame(shortName);
        final StringBuilder reminderString = new StringBuilder(
                String.format("-bangs pots together-\nGame time in t-minus %s\n", getStringTimeUntil(date)));
        for (String player : players) {
            reminderString.append(guild.getMemberById(player).getAsMention());
            reminderString.append(" ");
        }
        if (players.length == 0) {
            reminderString.append("(psst, if you add your players to the game they'll be @mentioned here)");
        }
        return reminderString.toString();
    }


    /**
     * @param shortName name of the game to find the time of
     * @return the time of the next session
     * @throws BadUserInputException if the game is not found
     * @throws BadStateException if the game time is non-existent or in the past
     */
    public static ZonedDateTime getNextSession(@NotNull String shortName) {
        return (ZonedDateTime) sessionsDatabaseTable.select(
                new Args(sessionsDatabaseTable, SessionsDatabaseFields.SHORT_NAME.fieldName, shortName),
                rs -> {
                    if (!rs.next()) {
                        throw new BadUserInputException("I can't seem to find the game " + shortName);
                    }
                    final String date = rs.getString(SessionsDatabaseFields.SESSION_TIME.fieldName);
                    if (date == null) {
                        throw new BadStateException("There is no time set for this session, ask the DM to add one");
                    }
                    final ZonedDateTime zonedDateTime;
                    try {
                        zonedDateTime = DatabaseTable.parseDateFromDatabase(date);
                    } catch (ParseException e) {
                        throw new ContactEwaException("Date parsing problem");
                    }
                    if (zonedDateTime.isBefore(ZonedDateTime.now())) {
                        throw new BadStateException("The game time entered is in the past, ask the DM to update it");
                    }
                    else {
                        return zonedDateTime;
                    }
                });
    }


    /**
     * @return an array of all the players in the given game
     * @throws BadUserInputException if the game is not found
     */
    private static String[] getPlayersInGame(@NotNull String shortName) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        return (String[]) playersDatabaseTable.select(
                new Args(playersDatabaseTable, SessionPlayersDatabaseFields.SHORT_NAME.fieldName, shortName), rs -> {
                    final List<String> players = new ArrayList<>();
                    while (rs.next()) {
                        players.add(rs.getString(SessionPlayersDatabaseFields.PLAYER.fieldName));
                    }
                    return players.toArray(new String[0]);
                });
    }


    /**
     * @return a string showing the time until the given date
     */
    private static String getStringTimeUntil(@NotNull ZonedDateTime date) {
        final long totalSeconds = ZonedDateTime.now().until(date, SECONDS);
        final TimeUnit timeUnitOfDifference = TimeUnit.SECONDS;
        return String.format("%d days, %d hrs %d mins %d secs", timeUnitOfDifference.toDays(totalSeconds),
                             timeUnitOfDifference.toHours(totalSeconds)
                                     - TimeUnit.DAYS.toHours(timeUnitOfDifference.toDays(totalSeconds)),
                             timeUnitOfDifference.toMinutes(totalSeconds)
                                     - TimeUnit.HOURS.toMinutes(timeUnitOfDifference.toHours(totalSeconds)),
                             timeUnitOfDifference.toSeconds(totalSeconds)
                                     - TimeUnit.MINUTES.toSeconds(timeUnitOfDifference.toMinutes(totalSeconds)));
    }


    /**
     * @param dmID must only dm one game for which the reminder will be created for
     * @return string mentioning all players with a countdown to next session
     */
    public static String getSessionReminder(@NotNull String dmID, @NotNull Guild guild) {
        return getSessionReminderAux(getDmsOnlyGame(dmID), guild);
    }


    /**
     * Removes a game with the specified short name from the database
     *
     * @throws BadUserInputException if the game is not found
     */
    public static void deleteGame(@NotNull String shortName) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        sessionsDatabaseTable.delete(new Args(sessionsDatabaseTable,
                                              SessionsDatabaseFields.SHORT_NAME.fieldName, shortName));
        playersDatabaseTable.delete(new Args(playersDatabaseTable,
                                             SessionPlayersDatabaseFields.SHORT_NAME.fieldName, shortName));
    }


    /**
     * @return a list of all games in the database
     */
    public static String getGamesList() {
        final Args args = new Args(sessionsDatabaseTable);
        args.setOrderBy(SessionsDatabaseFields.FULL_NAME.fieldName, true);
        return (String) sessionsDatabaseTable.select(args, rs -> {
            final StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString(SessionsDatabaseFields.SHORT_NAME.fieldName));
                sb.append(" - ");
                sb.append(rs.getString(SessionsDatabaseFields.FULL_NAME.fieldName));
                sb.append("\n");
            }
            return sb.toString();
        });
    }


    public static void setAutoReminder(@NotNull String shortName, boolean value) {
        throw new FeatureUnavailableException();
    }


    public static boolean getAutoReminder(@NotNull String shortName) {
        throw new FeatureUnavailableException();
    }


    private enum SessionsDatabaseFields implements DatabaseTable.DatabaseField {
        SESSION_TIME("sessionTime", DatabaseTable.SQLType.DATE, false),
        SHORT_NAME("shortName", DatabaseTable.SQLType.TEXT, true),
        FULL_NAME("fullName", DatabaseTable.SQLType.TEXT, true),
        DM_ID("dmID", DatabaseTable.SQLType.TEXT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        SessionsDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }



    private enum SessionPlayersDatabaseFields implements DatabaseTable.DatabaseField {
        SHORT_NAME("shortName", DatabaseTable.SQLType.TEXT, true), PLAYER("player", DatabaseTable.SQLType.TEXT, true);

        private String fieldName;
        private DatabaseTable.SQLType sqlType;
        private boolean required;


        SessionPlayersDatabaseFields(String fieldName, DatabaseTable.SQLType sqlType, boolean required) {
            this.fieldName = fieldName;
            this.sqlType = sqlType;
            this.required = required;
        }


        @Override
        public String getFieldName() {
            return fieldName;
        }


        @Override
        public DatabaseTable.SQLType getSqlType() {
            return sqlType;
        }


        @Override
        public boolean isRequired() {
            return required;
        }
    }
}
