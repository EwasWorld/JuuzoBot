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
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM '-' HH:mm z");


    private String fullName;
    private Date gameTime;


    private SessionTimes(String fullName) {
        this.fullName = fullName;
    }


    public static void addSessionTime(Member author, MessageChannel channel, String date) {
        String gameName = "";
        for (Role role : author.getRoles()) {
            if (gameTimes.containsKey(role.getName().toUpperCase())) {
                gameName = role.getName().toUpperCase();
                break;
            }
        }
        if (gameName.equals("")) {
            channel.sendMessage(
                    "You do not have a session role or your session is not in the database, pm a mod for help"
            ).queue();
            return;
        }

        try {
            gameTimes.get(gameName).gameTime = setDateFormat.parse(date);
            channel.sendMessage(
                    "New session time added " + printDateFormat.format(gameTimes.get(gameName).gameTime)
            ).queue();
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
        String roleName = message.split(" ")[0];
        String fullName = message.substring(roleName.length() + 1);

        gameTimes.put(roleName.toUpperCase(), new SessionTimes(fullName));
        channel.sendMessage("Game added").queue();
    }


    public static void getSessionTime(Member author, MessageChannel channel) {
        String sessionName = "";
        for (Role role : author.getRoles()) {
            if (gameTimes.containsKey(role.getName().toUpperCase())) {
                sessionName = role.getName().toUpperCase();
                break;
            }
        }
        if (sessionName.equals("")) {
            channel.sendMessage(
                    "You do not have a session role or your session is not in the database, pm a mod for help"
            ).queue();
            return;
        }
        Date gameTime = gameTimes.get(sessionName).gameTime;
        if (gameTime != null && gameTime.getTime() > System.currentTimeMillis()) {
            channel.sendMessage(
                        "Next session for %s is %s\n That's in %s",
                        gameTimes.get(sessionName).fullName, printDateFormat.format(gameTime),
                        getStringTimeUntil(gameTime)
            ).queue();
        }
        else {
            channel.sendMessage(String.format("There seems to be no session time for %s. Ask your dm to update it",
                                              gameTimes.get(sessionName).fullName)).queue();
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
