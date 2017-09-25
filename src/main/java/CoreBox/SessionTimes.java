package CoreBox;

import ExceptionsBox.BadStateException;
import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;



public class SessionTimes implements Serializable {
    private static final String fileName = "SessionTimes.txt";
    private static Map<String, SessionTimes> gameTimes = new HashMap<>();
    private static DateFormat setDateFormat = new SimpleDateFormat("HH:mm dd/M/yy z");
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");


    private String roleName;
    private String fullName;
    private Date gameTime;
    private boolean reminderHappened;


    protected SessionTimes() {
    }


    private SessionTimes(String roleName, String fullName) {
        this.roleName = roleName;
        this.fullName = fullName;
        reminderHappened = true;
    }


    static Map<String, SessionTimes> getGameTimes() {
        return gameTimes;
    }


    /*
     * Add the time/date to the database under the session role the member is assigned to
     * Game must have already been added to the Map using addGame())
     * date should be the same format as setDateFormat
     */
    public static String addSessionTime(Member author, String date) {
        final SessionTimes gameName = gameTimes.get(getRoleName(getSessionRole(author)));

        try {
            gameName.gameTime = setDateFormat.parse(date);
            gameName.reminderHappened = false;
            return String.format(
                    "New session time for %s added %s",
                    gameName.fullName,
                    printDateFormat.format(gameName.gameTime)
            );
        } catch (ParseException e) {
            throw new BadUserInputException(
                    "Bad date format, please use 'HH:mm dd/M/yy z'\n"
                            + "e.g. '16:00 21/8/17 BST'\n"
                            + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)"
            );
        }
    }


    private static String getRoleName(Role role) {
        return role.getName().toUpperCase();
    }


    /*
     * Find the session role for the given member
     */
    private static Role getSessionRole(Member author) {
        for (Role role : author.getRoles()) {
            if (gameTimes.containsKey(getRoleName(role))) {
                return role;
            }
        }
        throw new BadStateException(
                "You do not have a session role or your session is not in the database, pm a mod for help"
        );
    }


    /*
     * Removes a game with a specified short name from the map
     */
    public static void removeGame(String game) {
        if (game.contains(game.toUpperCase())) {
            gameTimes.remove(game.toUpperCase());
        }
        else {
            throw new BadUserInputException("Game does not exist");
        }
    }


    /*
     * Adds a game to the map so that session times can be added
     * message should contain the short game name first then the full name of the game
     */
    public static void addGame(String message) {
        if (message.split(" ").length >= 2) {
            final String roleName = message.split(" ")[0].toUpperCase();
            final String fullName = message.substring(roleName.length() + 1);

            if (gameTimes.containsKey(roleName)) {
                throw new BadUserInputException("Game with the name " + roleName + " already exists");
            }

            gameTimes.put(roleName, new SessionTimes(roleName, fullName));
        }
        else {
            throw new BadUserInputException("Incorrect format. Please provide a role name and a date");
        }
    }


    /*
     * Returns a string containing the next session time and a countdown until the session
     */
    public static String getNextSessionAsString(Member author) {
        return getNextSessionAsString(TimeZone.getDefault(), author);
    }


    /*
     * Returns a string containing the next session time and a countdown until the session
     */
    public static String getNextSessionAsString(TimeZone timezone, Member author) {
        final String sessionName = getRoleName(getSessionRole(author));
        final Date gameTime = getNextSessionTime(sessionName);

        printDateFormat.setTimeZone(timezone);
        return String.format(
                "Next session for %s is %s. That's in %s",
                gameTimes.get(sessionName).fullName, printDateFormat.format(gameTime),
                getStringTimeUntil(gameTime)
        );
    }


    /*
     * Returns the next session time for the specified session name provided that time is in the future
     */
    private static Date getNextSessionTime(String sessionName) {
        if (gameTimes.containsKey(sessionName)) {
            final Date gameTime = gameTimes.get(sessionName).gameTime;

            if (gameTime != null) {
                // Check the session is not in the past
                if (gameTime.after(new Date())) {
                    return gameTime;
                }
                else {
                    gameTimes.get(sessionName).gameTime = null;
                }
            }
        }

        throw new BadStateException(String.format(
                "There seems to be no session time for %s. Ask your dm to update it",
                gameTimes.get(sessionName.toUpperCase()).fullName
        ));
    }


    /*
     * Returns a string showing the time until the given date
     */
    private static String getStringTimeUntil(Date date) {
        final long millis = date.getTime() - System.currentTimeMillis();
        return String.format(
                "%d days, %d hrs %d mins %d secs",
                TimeUnit.MILLISECONDS.toDays(millis),
                TimeUnit.MILLISECONDS.toHours(millis)
                        - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }


    /*
     * Returns string containing a mention to all players and a countdown to the next session
     */
    public static String getSessionReminder(Member author) {
        return getSessionReminder(getSessionRole(author));
    }


    /*
     * Returns string containing a mention to all players and a countdown to the next session
     */
    public static String getSessionReminder(Role sessionRole) {
        final Date gameTime = getNextSessionTime(getRoleName(sessionRole));

        return String.format(
                "%s -bangs pots together-\nGame time in t-minus %s",
                sessionRole.getAsMention(), getStringTimeUntil(gameTime)
        );
    }


    public static void clearGameInformation() {
        gameTimes = new HashMap<>();
    }


    public static int size() {
        return gameTimes.size();
    }


    public static void save() {
        try {
            DataPersistence.save(fileName, gameTimes);
        } catch (IllegalStateException e) {
            System.out.println("Session times save failed");
        }
    }


    public static void load() {
        try {
            gameTimes = (Map<String, SessionTimes>) DataPersistence.loadFirstObject(fileName);
        } catch (IllegalStateException e) {
            System.out.println("Session times load failed");
        }
    }


    String getRoleName() {
        return roleName;
    }


    Date getGameTime() {
        return gameTime;
    }


    boolean isReminderHappened() {
        return reminderHappened;
    }


    void setReminderHappened(boolean reminderHappened) {
        this.reminderHappened = reminderHappened;
    }
}
