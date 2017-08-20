package Foo;

import DataPersistenceBox.DataPersistence;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class SessionTimes implements Serializable {
    private static final String fileName = "SessionTimes.txt";
    private static Map<String, SessionTimes> gameTimes = new HashMap<>();
    private static DateFormat setDateFormat = new SimpleDateFormat("HH:mm dd/M/yy z");
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");


    private String fullName;
    private Date gameTime;


    private SessionTimes(String fullName) {
        this.fullName = fullName;
    }


    /*
     * Add the time/date to the database under the session role the member is assigned to
     * Game must have already been added to the Map using addGame())
     * date should be the same format as setDateFormat
     */
    public static String addSessionTime(Member author, String date) {
        String gameName = getRoleName(getSessionRole(author));

        try {
            gameTimes.get(gameName).gameTime = setDateFormat.parse(date);
            return String.format(
                    "New session time for %s added %s",
                    gameTimes.get(gameName).fullName,
                    printDateFormat.format(gameTimes.get(gameName).gameTime)
            );
        } catch (ParseException e) {
            throw new IllegalArgumentException(
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
        throw new IllegalStateException(
                "You do not have a session role or your session is not in the database, pm a mod for help"
        );
    }


    /*
     * Removes a game with a specified short name from the map
     */
    public static String removeGame(String game) {
        if (game.contains(game.toUpperCase())) {
            gameTimes.remove(game.toUpperCase());
            return "Game removed";
        }
        else {
            throw new IllegalArgumentException("Game doesn't exist therefore wasn't removed");
        }
    }


    /*
     * Adds a game to the map so that session times can be added
     * message should contain the short game name first then the full name of the game
     */
    public static String addGame(String message) {
        if (message.split(" ").length >= 2) {
            String roleName = message.split(" ")[0].toUpperCase();
            String fullName = message.substring(roleName.length() + 1);

            if (gameTimes.containsKey(roleName)) {
                throw new IllegalArgumentException("Game with the name " + roleName + " already exists");
            }

            gameTimes.put(roleName, new SessionTimes(fullName));
            return "Game added";
        }
        else {
            throw new IllegalArgumentException("Incorrect format. Please provide a role name and a date");
        }
    }


    /*
     * Returns a string containing the next session time and a countdown until the session
     */
    public static String getNextSessionAsString(Member author) {
        final String sessionName = getRoleName(getSessionRole(author));
        final Date gameTime = getNextSessionTime(sessionName);

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
        try {
            final Date gameTime = gameTimes.get(sessionName).gameTime;

            if (gameTime.getTime() > System.currentTimeMillis()) {
                return gameTime;
            }
        } catch (NullPointerException e) {
            // Thrown if the game is not found in gameTimes. The below exception should be thrown instead
        }

        throw new IllegalStateException(String.format(
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
        final Role sessionRole = getSessionRole(author);
        final Date gameTime = getNextSessionTime(getRoleName(sessionRole));

        return String.format(
                "%s -bangs pots together-\nGame time in t-minus %s",
                sessionRole.getAsMention(), getStringTimeUntil(gameTime)
        );
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
}
