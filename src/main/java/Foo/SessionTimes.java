package main.java.Foo;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;



public class SessionTimes implements Serializable {
    private static final String fileLocation = IDs.mainFilePath + "Foo/SessionTimes.txt";
    private static Map<Long, Date> gameTimes = new HashMap<>();
    private static Set<String> games = new HashSet<>();
    private static DateFormat setDateFormat = new SimpleDateFormat("HH:mm dd/M/yy z");
    private static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM '-' HH:mm z");


    public static void addSessionTime(Member author, MessageChannel channel, String date) {
        long sessionID = 0;
        for (Role role : author.getRoles()) {
            if (games.contains(role.getName().toUpperCase())) {
                sessionID = role.getIdLong();
            }
        }
        if (sessionID == 0) {
            channel.sendMessage("You do not have a session role, pm a mod for one").queue();
            return;
        }

        try {
            gameTimes.put(sessionID, setDateFormat.parse(date));
            channel.sendMessage("New session time added " + printDateFormat.format(gameTimes.get(sessionID))).queue();
        } catch (ParseException e) {
            channel.sendMessage("Bad date format, please use 'HH:mm dd/M/yy z'\n"
                                        + "e.g. '16:00 21/8/17 BST'\n"
                                        + "  or '16:00 21/8/17 GMT + 1' (spaces around '+' are important)").queue();
        }
    }

    // TODO: Remove game


    public static void addGame(MessageChannel channel, String game) {
        games.add(game.toUpperCase());
        channel.sendMessage("Game added").queue();
    }


    public static void getSessionTime(Member author, MessageChannel channel) {
        long sessionID = 0;
        String sessionName = "";
        for (Role role : author.getRoles()) {
            sessionName = role.getName().toUpperCase();

            if (games.contains(sessionName)) {
                sessionID = role.getIdLong();
            }
        }
        if (sessionID == 0) {
            channel.sendMessage("You do not have a session role, pm a mod for one").queue();
            return;
        }
        if (gameTimes.containsKey(sessionID)) {
            Date sessionTime = gameTimes.get(sessionID);
            String messageString = String.format(
                    "Next session for %s is %s\n", sessionName, printDateFormat.format(sessionTime));
            messageString += "That's in " + getStringOfTimeToNow(sessionTime);
            channel.sendMessage(messageString).queue();
        }
        else {
            channel.sendMessage("There seems to be no session time for " + sessionName).queue();
        }
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
            gameTimes = (Map<Long, Date>) objects.get(0);
            games = (Set<String>) objects.get(1);
        } catch (IllegalStateException e) {
            channel.sendMessage("Session times load failed").queue();
        }
    }

    private static String getStringOfTimeToNow(Date date1) {
        long dateDifSec = Math.abs(date1.getTime() - System.currentTimeMillis());
        long dateDifMin = Math.floorDiv(dateDifSec, 60);
        long dateDifHr = Math.floorDiv(dateDifMin, 60);
        long dateDifDay = Math.floorDiv(dateDifHr, 24);

        dateDifSec -= dateDifMin * 60;
        dateDifMin -= dateDifHr * 60;
        dateDifHr -= dateDifDay * 24;

        return dateDifDay + " day(s) " + dateDifHr + " hr(s) " + dateDifMin + " min(s) " + dateDifSec + "sec(s)";
    }
}
