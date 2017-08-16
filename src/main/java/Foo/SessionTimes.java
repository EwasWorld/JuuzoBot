package Foo;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;



public class SessionTimes implements Serializable {
    private static final String fileLocation = IDs.mainFilePath + "Foo/SessionTimes.txt";
    private static Map<String, SessionTimes> gameTimes = new HashMap<>();
    private static DateFormat setDateFormat = new SimpleDateFormat("HH:mm dd/M/yy z");
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");


    private String fullName;
    private Date gameTime;


    private SessionTimes(String fullName) {
        this.fullName = fullName;
    }


    public static void addSessionTime(Member author, MessageChannel channel, String date) {
        String gameName = getRoleName(getSessionRole(author));

        try {
            gameTimes.get(gameName).gameTime = setDateFormat.parse(date);
            channel.sendMessage(String.format(
                    "New session time for %s added %s",
                    gameTimes.get(gameName).fullName,
                    printDateFormat.format(gameTimes.get(gameName).gameTime)
            )).queue();
        } catch (ParseException e) {
            channel.sendMessage("Bad date format, please use 'HH:mm dd/M/yy z'\n"
                                        + "e.g. '16:00 21/8/17 BST'\n"
                                        + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)").queue();
        }
    }


    public static void removeGame(MessageChannel channel, String game) {
        if (game.contains(game.toUpperCase())) {
            gameTimes.remove(game.toUpperCase());
            channel.sendMessage("Game removed").queue();
        }
        else {
            channel.sendMessage("Game doesn't exist therefore wasn't removed").queue();
        }
    }


    public static void addGame(MessageChannel channel, String message) {
        String roleName = message.split(" ")[0].toUpperCase();
        String fullName = message.substring(roleName.length() + 1);

        gameTimes.put(roleName, new SessionTimes(fullName));
        channel.sendMessage("Game added").queue();
    }


    public static Date getSessionTime(String sessionName) {
        try {
            final Date gameTime = gameTimes.get(sessionName).gameTime;

            if (gameTime.getTime() > System.currentTimeMillis()) {
                return gameTime;
            }
        } catch (Exception e) {
            // Thrown if the game is not found in gameTimes. The below exception should be thrown instead
        }

        throw new IllegalStateException(String.format(
                "There seems to be no session time for %s. Ask your dm to update it",
                gameTimes.get(sessionName.toUpperCase()).fullName
        ));
    }


    private static Role getSessionRole(Member author) {
        for (Role role : author.getRoles()) {
            if (gameTimes.containsKey(getRoleName(role))) {
                return role;
            }
        }
        throw new IllegalStateException(
                "You do not have a session role or your session is not in the database, pm a mod for help");
    }


    public static void getSessionTime(Member author, MessageChannel channel) {
        try {
            final String sessionName = getRoleName(getSessionRole(author));
            final Date gameTime = getSessionTime(sessionName);

            channel.sendMessage(
                    "Next session for %s is %s. That's in %s",
                    gameTimes.get(sessionName).fullName, printDateFormat.format(gameTime),
                    getStringTimeUntil(gameTime)
            ).queue();
        } catch (IllegalStateException e) {
            channel.sendMessage(e.getMessage()).queue();
        }
    }


    private static String getRoleName(Role role) {
        return role.getName().toUpperCase();
    }


    public static void getSessionReminder(Member author, MessageChannel channel) {
        try {
            final Role sessionRole = getSessionRole(author);
            final Date gameTime = getSessionTime(getRoleName(sessionRole));

            channel.sendMessage(
                    "%s -bangs pots together-\nGame time in t-minus %s", sessionRole.getAsMention(),
                    getStringTimeUntil(gameTime)
            ).queue();
        } catch (IllegalStateException e) {
            channel.sendMessage(e.getMessage()).queue();
        }
    }


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


    public static void save() {
        try {
            LoadSaveConstants.save(fileLocation, gameTimes);
        } catch (IllegalStateException e) {
            System.out.println("Session times save failed");
        }
    }


    public static void load() {
        try {
            gameTimes = (Map<String, SessionTimes>) LoadSaveConstants.loadFirstObject(fileLocation);
        } catch (IllegalStateException e) {
            System.out.println("Session times load failed");
        }
    }
}
