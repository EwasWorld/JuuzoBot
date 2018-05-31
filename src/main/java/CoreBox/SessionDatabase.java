package CoreBox;

import ExceptionsBox.BadUserInputException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;



public class SessionDatabase {
    public static void addGameToDatabase(String shortName, String fullName, String dmID) {
        final Connection connection = Database.openConnection();
        String sql = "INSERT INTO SessionInfo(shortName, fullName, dmID) VALUES(?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shortName.toUpperCase());
            ps.setString(2, fullName);
            ps.setString(3, dmID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    /*
     * Updates the session associated with shortName (as long as the dmID matches) with a date for the next session
     * Returns the name of the session updated
     */
    public static String addSessionTime(String dmID, String shortName, Date date) {
        try {
            // TODO Fix make sure this works
            if (!getShortNames(dmID).contains(shortName)) {
                throw new BadUserInputException("You don't dm this game");
            }
            addSessionTimeToSpecificSession(shortName, date);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }

        return shortName;
    }


    /*
     * Updates the session associated with the dm (as long as there's only one session they dm) with a date for the next session
     * Returns the name of the session updated
     */
    public static String addSessionTime(String dmID, Date date) {
        final String shortName;
        try {
            final List<String> shortNames = getShortNames(dmID);
            // TODO Fix make sure this works
            if (shortNames.size() == 0) {
                throw new BadUserInputException("You don't dm any games");
            }
            else if (shortNames.size() != 1) {
                throw new BadUserInputException("You dm more than 1 game, specify which you want to update");
            }
            shortName = shortNames.get(0);
            addSessionTimeToSpecificSession(shortName, date);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }

        return shortName;
    }


    /*
     * Get the short names of all the games that the given DM runs
     */
    private static List<String> getShortNames(String dmID) throws SQLException {
        final Connection connection = Database.openConnection();
        String sql = "SELECT shortName FROM SessionInfo WHERE dmID=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dmID);
            final ResultSet rs = ps.executeQuery();

            final List<String> shortNames = new ArrayList<>();
            while (rs.next()) {
                shortNames.add(rs.getString("shortName").toUpperCase());
            }
            return shortNames;
        }
    }


    /*
     * Updates the nextSession time for a given session
     */
    public static void addSessionTimeToSpecificSession(String shortName, Date date) {
        final Connection connection = Database.openConnection();
        String sql1 = "UPDATE SessionInfo SET nextSession=? WHERE shortName=?";
        String sql2 = "UPDATE SessionInfo SET autoReminderSet=1 WHERE shortName=?";

        try (PreparedStatement ps1 = connection.prepareStatement(sql1);
                PreparedStatement ps2 = connection.prepareStatement(sql2)) {
            ps1.setString(1, Database.setDateFormat.format(date));
            ps1.setString(2, shortName.toUpperCase());
            ps1.executeUpdate();
            ps2.setString(1, shortName.toUpperCase());
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*
     * Add a given player to a given game
     */
    public static void addPlayer(String shortName, String playerID) {
        final Connection connection = Database.openConnection();
        String sql = "INSERT INTO SessionPlayers(shortName, playerID) VALUES(?,?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shortName.toUpperCase());
            ps.setString(2, playerID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    static Date getNextSession(String shortName) {
        final Connection connection = Database.openConnection();
        final String sql = "SELECT nextSession FROM SessionInfo WHERE shortName=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shortName);
            final ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dateStr = rs.getString("nextSession");
                return getDateFromStringPossibleNull(dateStr);
            }
            else {
                throw new IllegalStateException("Short name not in database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    // TODO Fix this feels wrong, fix it
    private static Date getDateFromStringPossibleNull(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        else {
            return parseDateFromDatabase(dateStr);
        }
    }


    private static Date parseDateFromDatabase(String date) {
        try {
            return Database.setDateFormat.parse(date);
        } catch (ParseException e) {
            // Date cannot be invalid because it is set by formatting a Date object
            throw new IllegalStateException("Dafak did you do?");
        }
    }


    /*
     * Returns a map<shortName, nextSession) for all games the player takes part in
     */
    public static Map<String, java.util.Date> getNextSessionTime(String playerID) {
        final Connection connection = Database.openConnection();
        final Map<String, Date> map = new HashMap<>();
        final String sql1 = "SELECT shortName FROM SessionPlayers WHERE playerID=?";
        final String sql2 = "SELECT shortName, nextSession FROM SessionInfo WHERE dmID=?";

        try (PreparedStatement ps1 = connection.prepareStatement(sql1);
                PreparedStatement ps2 = connection.prepareStatement(sql2))
        {
            ps1.setString(1, playerID);
            ps2.setString(1, playerID);
            final ResultSet rs1 = ps1.executeQuery();
            final ResultSet rs2 = ps2.executeQuery();

            while (rs1.next()) {
                final String shortName = rs1.getString("shortName");
                map.put(shortName, getNextSession(shortName));
            }
            while (rs2.next()) {
                map.put(rs2.getString("shortName" + "*"),
                        parseDateFromDatabase(rs2.getString("nextSession")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }


    /*
     * Gives a string representation of all records in SessionInfo and SessionPlayers
     * Used for debugging
     */
    public static String databaseToString() {
        final Connection connection = Database.openConnection();

        final String sql1 = "SELECT * FROM SessionInfo";
        final String sql2 = "SELECT * FROM SessionPlayers";
        final StringBuilder outString = new StringBuilder();

        try (PreparedStatement ps1 = connection.prepareStatement(sql1);
             PreparedStatement ps2 = connection.prepareStatement(sql2))
        {
            final ResultSet rs1 = ps1.executeQuery();
            final ResultSet rs2 = ps2.executeQuery();

            while (rs1.next()) {
                outString.append(rs1.getInt("id"));
                outString.append(", ");
                outString.append(rs1.getString("shortName"));
                outString.append(", ");
                outString.append(rs1.getString("fullName"));
                outString.append(", ");
                outString.append(rs1.getString("dmID"));
                outString.append(", ");
                outString.append(rs1.getString("nextSession"));

                outString.append("\n");
            }

            while (rs2.next()) {
                outString.append("\n");
                outString.append(rs2.getInt("id"));
                outString.append(", ");
                outString.append(rs2.getString("playerID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return outString.toString();
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
     * Returns string mentioning all players with a countdown to next session
     * Ensures the dmID is correct
     */
    public static String getSessionReminder(String shortName, String dmID, Guild guild) {
        try {
            // TODO Fix make sure this works
            if (!getShortNames(dmID).contains(shortName)) {
                throw new BadUserInputException("You don't dm this game");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getSessionReminderAux(shortName, guild);
    }


    /*
     * Returns string mentioning all players with a countdown to next session
     * Ensures the dmID is correct
     * DM must only DM one game
     */
    public static String getSessionReminder(String dmID, Guild guild) {
        List<String> shortNames;
        try {
            shortNames = getShortNames(dmID);
            // TODO Fix make sure this works
            if (getShortNames(dmID).size() == 0) {
                throw new BadUserInputException("You don't dm any games");
            }
            else if (getShortNames(dmID).size() != 1) {
                // TODO remind for the closest game?
                throw new BadUserInputException("You dm more than one game, please specify");
            }

            return getSessionReminderAux(shortNames.get(0), guild);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /*
     * Returns string containing a mention to all players in the game
     *      and a countdown to the next session
     */
    private static String getSessionReminderAux(String shortName, Guild guild) {
        try {
            final List<String> playerIDs = getPlayerIDs(shortName);
            final List<Member> members = getMembers(guild, playerIDs);
            final Date nextSession = getNextSession(shortName);

            final StringBuilder reminderString = new StringBuilder(
                    String.format("-bangs pots together-\nGame time in t-minus %s\n", getStringTimeUntil(nextSession)));
            for (Member member : members) {
                reminderString.append(member.getAsMention() + " ");
            }

            return reminderString.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*
     * Get the ID of every player in the given game
     */
    private static List<String> getPlayerIDs(String shortName) throws SQLException {
        final Connection connection = Database.openConnection();
        final String sql = "SELECT playerID FROM SessionPlayers WHERE shortName=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shortName);
            final ResultSet rs = ps.executeQuery();

            final List<String> playerIDs = new ArrayList<>();
            while (rs.next()) {
                playerIDs.add(rs.getString("playerID"));
            }

            return playerIDs;
        }
    }


    /*
     * Get the user objects from the guild (specified in IDs class) for every listed user ID
     * TODO Optimisation should this be somewhere else? Like in a guild class?
     */
    private static List<Member> getMembers(Guild guild, List<String> ids) {
        final List<Member> members = new ArrayList<>();
        for (String id : ids) {
            members.add(guild.getMemberById(id));
        }
        return members;
    }


    /*
     * Removes a game with the specified short name from the database
     */
    public static void removeGame(String shortName) {
        final Connection connection = Database.openConnection();

        String sqlInfo = "DELETE FROM SessionInfo WHERE shortName=?";
        String sqlPlayers = "DELETE FROM SessionPlayers WHERE shortName=?";

        try (PreparedStatement psInfo = connection.prepareStatement(sqlInfo);
             PreparedStatement psPlayers = connection.prepareStatement(sqlPlayers))
        {
            psInfo.setString(1, shortName);
            psPlayers.setString(1, shortName);
            psInfo.executeUpdate();
            psPlayers.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*
     * Lists all games in the database
     */
    public static String getGamesList() {
        final Connection connection = Database.openConnection();

        final StringBuilder stringBuilder = new StringBuilder("");
        final String sql = "SELECT shortName, fullName FROM SessionInfo";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)
        )
        {
            while (rs.next()) {
                stringBuilder.append(rs.getString("shortName"));
                stringBuilder.append(" - ");
                stringBuilder.append(rs.getString("fullName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }


    public static void setAutoReminder(String shortName, boolean value) {
        // TODO
    }


    public static boolean getAutoReminder(String shortName) {
        // TODO
        return false;
    }
}
