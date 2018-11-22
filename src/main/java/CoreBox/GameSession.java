package CoreBox;

import DatabaseBox.DatabaseTable;
import DatabaseBox.PrimaryKey;
import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import ExceptionsBox.FeatureUnavailableException;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.ResultSet;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.SECONDS;



/**
 * refactored 22/11/18
 * TODO Optimisation reduce the number of database calls for things like checking shortnames (try to have it only
 * happen once for any action)
 */
public class GameSession {
    private static final DatabaseTable sessionsDatabaseTable = DatabaseTable.createDatabaseTable(
            "Sessions", SessionsDatabaseFields.values(), new PrimaryKey(Collections.singleton(
                    SessionsDatabaseFields.SHORT_NAME.fieldName)));
    private static final DatabaseTable sessionPlayersDatabaseTable = DatabaseTable.createDatabaseTable(
            "SessionPlayers", SessionPlayersDatabaseFields.values());


    public static void deleteAllTables() {
        if (!DatabaseTable.isInTestMode()) {
            throw new IllegalStateException("This action can only be taken in test mode");
        }
        else {
            sessionsDatabaseTable.deleteTable();
            sessionPlayersDatabaseTable.deleteTable();
        }
    }


    public static int[] getRowCounts() {
        return new int[]{sessionsDatabaseTable.getRowCount(), sessionPlayersDatabaseTable.getRowCount()};
    }


    /**
     * @param shortName Ideally a shorter version of the full name and easy to type and can be used to identify the game
     * @param fullName the full name of the game
     * @param dmID the ID of the DM
     * @throws BadUserInputException if the short name is not unique
     */
    public static void addGameToDatabase(String shortName, String fullName, String dmID) {
        if (shortName.equals("")) {
            throw new BadUserInputException("Short name cannot be nothing");
        }
        if (shortNameExists(shortName)) {
            throw new BadUserInputException(
                    "There's already a game with that short name, you'll have to choose another I'm afraid");
        }

        final Map<String, Object> args = new HashMap<>();
        args.put(SessionsDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        args.put(SessionsDatabaseFields.FULL_NAME.getFieldName(), fullName);
        args.put(SessionsDatabaseFields.DM_ID.getFieldName(), dmID);
        sessionsDatabaseTable.insert(args);
    }


    private static boolean shortNameExists(String shortName) {
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionsDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        return (boolean) sessionsDatabaseTable.selectAND(args, ResultSet::next);
    }


    /**
     * Updates the session associated with shortName with a date for the next session
     *
     * @param userID the user who wants to add a session time
     * @throws BadUserInputException if the game specified does not belong to the user
     */
    public static void addSessionTime(String userID, String shortName, ZonedDateTime date) {
        if (!getDmId(shortName).equals(userID)) {
            throw new BadUserInputException("This isn't your game, only the DM can change the session time");
        }
        addSessionTimeToSpecificSession(shortName, date);
    }


    /**
     * @return the id of the dm associated with the shortname
     * @throws BadUserInputException if the game is not found
     */
    private static String getDmId(String shortName) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionsDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        return (String) sessionsDatabaseTable.selectAND(args, rs -> {
            rs.next();
            return rs.getString(SessionsDatabaseFields.DM_ID.getFieldName());
        });
    }


    /**
     * Updates the next session time for a given session
     */
    private static void addSessionTimeToSpecificSession(String shortName, ZonedDateTime date) {
        final Map<String, Object> setArgs = new HashMap<>();
        setArgs.put(SessionsDatabaseFields.SESSION_TIME.getFieldName(), date);
        final Map<String, Object> whereArgs = new HashMap<>();
        whereArgs.put(SessionsDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        sessionsDatabaseTable.updateAND(setArgs, whereArgs);
    }


    /**
     * Updates the session associated with the dm with a date for the next session
     *
     * @return the shortname of the session the time has been added to
     * @throws BadUserInputException if the dm does not dm exactly one game
     */
    public static String addSessionTime(String dmID, ZonedDateTime date) {
        final String shortName = getDmsOnlyGame(dmID);
        addSessionTimeToSpecificSession(shortName, date);
        return shortName;
    }


    /**
     * @return the shortName of the only game the dmID runs
     * @throws BadUserInputException if the dm does not dm exactly one game
     */
    private static String getDmsOnlyGame(String dmID) {
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
    private static String[] getDmGamesShortNames(String dmID) {
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionsDatabaseFields.DM_ID.getFieldName(), dmID);
        return (String[]) sessionsDatabaseTable.selectAND(args, rs -> {
            final List<String> shortNames = new ArrayList<>();
            while (rs.next()) {
                shortNames.add(rs.getString(SessionsDatabaseFields.SHORT_NAME.getFieldName()));
            }
            return shortNames.toArray(new String[0]);
        });
    }


    /**
     * Add a player to a game
     *
     * @throws BadUserInputException if the game is not found or if the player is already in the game
     */
    public static void addPlayer(String shortName, String playerID) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionPlayersDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        args.put(SessionPlayersDatabaseFields.PLAYER.getFieldName(), playerID);
        sessionPlayersDatabaseTable.selectAND(args, rs -> {
            if (rs.next()) {
                throw new BadUserInputException("This player has already been added to the game");
            }
            return null;
        });
        sessionPlayersDatabaseTable.insert(args);
    }


    /**
     * TODO Sort sessions by next session (soonest first)
     *
     * @return All games the player takes part in with their next session time or n/a
     * @throws BadUserInputException if the game is not found
     */
    public static String getAllSessionTimes(String playerID) {
        final String[] playerGamesArray = getPlayerGamesShortNames(playerID);
        final String[] dmGamesArray = getDmGamesShortNames(playerID);
        if (playerGamesArray.length == 0 && dmGamesArray.length == 0) {
            throw new BadUserInputException("You don't seem to be added to any games (or DMing any)");
        }

        final String format = "%s%s - %s\n";
        final String dmStar = "*";
        final List<String> playerGames = new LinkedList<>(Arrays.asList(playerGamesArray));
        final List<String> dmGames = new LinkedList<>(Arrays.asList(dmGamesArray));
        return (String) sessionsDatabaseTable.selectAll(rs -> {
            final StringBuilder sb = new StringBuilder("Note: name* means you DM this game\n");
            while (rs.next()) {
                final String shortName = rs.getString(SessionsDatabaseFields.SHORT_NAME.getFieldName());
                boolean isDmGame = dmGames.contains(shortName);
                if (playerGames.contains(shortName) || isDmGame) {
                    String dmStarUse = "";
                    if (isDmGame) {
                        dmStarUse = dmStar;
                    }
                    final String date = rs.getString(SessionsDatabaseFields.SESSION_TIME.getFieldName());
                    if (date != null) {
                        sb.append(String.format(format, shortName, dmStarUse,
                                                DatabaseTable.databaseStringToPrintableString(date)));
                    }
                    else {
                        sb.append(String.format(format, shortName, dmStarUse, "n/a"));
                    }
                    playerGames.remove(shortName);
                    dmGames.remove(shortName);
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        });
    }


    private static String[] getPlayerGamesShortNames(String playerID) {
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionPlayersDatabaseFields.PLAYER.getFieldName(), playerID);
        return (String[]) sessionPlayersDatabaseTable.selectAND(args, rs -> {
            final List<String> shortNames = new ArrayList<>();
            while (rs.next()) {
                shortNames.add(rs.getString(SessionPlayersDatabaseFields.SHORT_NAME.getFieldName()));
            }
            return shortNames.toArray(new String[0]);
        });
    }


    /**
     * @param userID user who made the call for the reminder must be the dm of the game
     * @return a string mentioning all players with a countdown to next session
     * @throws BadUserInputException if the game doesn't belong to the user who made the call
     */
    public static String getSessionReminder(String shortName, String userID, Guild guild) {
        if (!getDmId(shortName).equals(userID)) {
            throw new BadUserInputException("This isn't your game, only the DM can send a reminder");
        }
        return getSessionReminderAux(shortName, guild);
    }


    /**
     * @return a string containing a mention to all players in the game and a countdown to the next session
     * @throws BadStateException if there is no time for the session or the time is in the past
     */
    private static String getSessionReminderAux(String shortName, Guild guild) {
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
    public static ZonedDateTime getNextSession(String shortName) {
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionsDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        return (ZonedDateTime) sessionsDatabaseTable.selectAND(args, rs -> {
            if (rs.next()) {
                final String date = rs.getString(SessionsDatabaseFields.SESSION_TIME.getFieldName());
                if (date != null) {
                    final ZonedDateTime zonedDateTime = DatabaseTable.getDatabaseDateFromString(date);
                    if (zonedDateTime.isBefore(ZonedDateTime.now())) {
                        throw new BadStateException("The game time entered is in the past, ask the DM to update it");
                    }
                    else {
                        return zonedDateTime;
                    }
                }
                else {
                    throw new BadStateException("There is no time set for this session, ask the DM to add one");
                }
            }
            else {
                throw new BadUserInputException("I can't seem to find the game " + shortName);
            }
        });
    }


    /**
     * @return an array of all the players in the given game
     * @throws BadUserInputException if the game is not found
     */
    private static String[] getPlayersInGame(String shortName) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionPlayersDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        return (String[]) sessionPlayersDatabaseTable.selectAND(args, rs -> {
            final List<String> players = new ArrayList<>();
            while (rs.next()) {
                players.add(rs.getString(SessionPlayersDatabaseFields.PLAYER.getFieldName()));
            }
            return players.toArray(new String[0]);
        });
    }


    /**
     * @return a string showing the time until the given date
     */
    private static String getStringTimeUntil(ZonedDateTime date) {
        final long totalSeconds = ZonedDateTime.now().until(date, SECONDS);
        final TimeUnit timeUnitOfDifference = TimeUnit.SECONDS;
        return String.format("%d days, %d hrs %d mins %d secs", timeUnitOfDifference.toDays(totalSeconds),
                             timeUnitOfDifference.toHours(totalSeconds) - TimeUnit.DAYS
                                     .toHours(timeUnitOfDifference.toDays(totalSeconds)),
                             timeUnitOfDifference.toMinutes(totalSeconds) - TimeUnit.HOURS
                                     .toMinutes(timeUnitOfDifference.toHours(totalSeconds)),
                             timeUnitOfDifference.toSeconds(totalSeconds) - TimeUnit.MINUTES
                                     .toSeconds(timeUnitOfDifference.toMinutes(totalSeconds)));
    }


    /**
     * @param dmID must only dm one game for which the reminder will be created for
     * @return string mentioning all players with a countdown to next session
     */
    public static String getSessionReminder(String dmID, Guild guild) {
        return getSessionReminderAux(getDmsOnlyGame(dmID), guild);
    }


    /**
     * Removes a game with the specified short name from the database
     *
     * @throws BadUserInputException if the game is not found
     */
    public static void deleteGame(String shortName) {
        if (!shortNameExists(shortName)) {
            throw new BadUserInputException("I can't seem to find the game " + shortName);
        }
        final Map<String, Object> args = new HashMap<>();
        args.put(SessionsDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        sessionsDatabaseTable.deleteAND(args);
        final Map<String, Object> playersArgs = new HashMap<>();
        playersArgs.put(SessionPlayersDatabaseFields.SHORT_NAME.getFieldName(), shortName);
        sessionPlayersDatabaseTable.deleteAND(playersArgs);
    }


    /**
     * TODO Sort alphabetically
     *
     * @return a list of all games in the database
     */
    public static String getGamesList() {
        return (String) sessionsDatabaseTable.selectAll(rs -> {
            final StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString(SessionsDatabaseFields.SHORT_NAME.getFieldName()));
                sb.append(" - ");
                sb.append(rs.getString(SessionsDatabaseFields.FULL_NAME.getFieldName()));
                sb.append("\n");
            }
            return sb.toString();
        });
    }


    public static void setAutoReminder(String shortName, boolean value) {
        throw new FeatureUnavailableException();
    }


    public static boolean getAutoReminder(String shortName) {
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
