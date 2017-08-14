package main.java.Foo;

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
    private static Map<String, Date> gameTimes = new HashMap<>();
    private static Set<String> games = new HashSet<>();
    private static DateFormat setDateFormat = new SimpleDateFormat("HH:mm dd/M/yy z");
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM '-' HH:mm z");


    public static void addSessionTime(Member author, MessageChannel channel, String date) {
        String gameName = "";
        for (Role role : author.getRoles()) {
            if (games.contains(role.getName().toUpperCase())) {
                gameName = role.getName().toUpperCase();
                break;
            }
        }
        if (gameName.equals("")) {
            channel.sendMessage("You do not have a session role, pm a mod for one").queue();
            return;
        }

        try {
            gameTimes.put(gameName, setDateFormat.parse(date));
            channel.sendMessage("New session time added " + printDateFormat.format(gameTimes.get(gameName))).queue();
        } catch (ParseException e) {
            channel.sendMessage("Bad date format, please use 'HH:mm dd/M/yy z'\n"
                                        + "e.g. '16:00 21/8/17 BST'\n"
                                        + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)").queue();
        }
    }

    public static void removeGame(MessageChannel channel, String game) {
        if (game.contains(game.toUpperCase())) {
            games.remove(game.toUpperCase());
            channel.sendMessage("Game removed").queue();
        }
        else {
            channel.sendMessage("Game doesn't exist therefore wasn't removed").queue();
        }
    }


    public static void addGame(MessageChannel channel, String game) {
        games.add(game.toUpperCase());
        channel.sendMessage("Game added").queue();
    }


    public static void getSessionTime(Member author, MessageChannel channel) {
        String sessionName = "";
        for (Role role : author.getRoles()) {
            sessionName = role.getName().toUpperCase();
            if (games.contains(sessionName)) {
                break;
            }
            sessionName = "";
        }
        if (sessionName.equals("")) {
            channel.sendMessage("You do not have a session role, pm a mod for one").queue();
            return;
        }
        if (gameTimes.containsKey(sessionName)) {
            Date sessionTime = gameTimes.get(sessionName);

            if (sessionTime.getTime() > System.currentTimeMillis()) {
                String messageString = String.format(
                        "Next session for %s is %s\n", sessionName, printDateFormat.format(sessionTime));
                messageString += "That's in " + getStringOfTimeToNow(sessionTime);
                channel.sendMessage(messageString).queue();
            }
            else {
                channel.sendMessage(
                        "There appears to be no new session time for " + sessionName + ". Ask your dm to update it")
                        .queue();
            }
        }
        else {
            channel.sendMessage("There seems to be no session time for " + sessionName).queue();
        }
    }


    private static String getStringOfTimeToNow(Date date1) {
        final long millis = date1.getTime() - System.currentTimeMillis();
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


    public static void save(MessageChannel channel) {
        try {
            LoadSaveConstants.save(fileLocation, new Object[]{gameTimes, games});
        } catch (IllegalStateException e) {
            channel.sendMessage("Session times save failed").queue();
        }
    }


    public static void load(MessageChannel channel) {
        try {
            List<Object> objects = LoadSaveConstants.load(fileLocation);
            gameTimes = (Map<String, Date>) objects.get(0);
            games = (Set<String>) objects.get(1);
        } catch (IllegalStateException e) {
            channel.sendMessage("Session times load failed").queue();
        }
    }
}
