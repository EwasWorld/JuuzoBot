package CoreBox;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;



public class SessionDatabase {
    private static final String databaseFileLocation = Bot.getPathToJuuzoBot() + "Juuzo.db";
    private static final String url = "jdbc:sqlite:" + databaseFileLocation;
    private static final String placeholder = "000";
    public static String setDateFormatStr = "HH:mm dd/M/yy z";
    public static DateFormat setDateFormat = new SimpleDateFormat(setDateFormatStr);
    public static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");
    private static Connection connection = null;


    public static void clearTables() {
        openConnection();

        String infoSQL = "DROP TABLE IF EXISTS SessionInfo";
        String playersSQL = "DROP TABLE IF EXISTS SessionPlayers";


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(infoSQL);
            stmt.execute(playersSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        createNewTables();
    }


    private static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void addGameToDatabase(String shortName, String fullName, String dmID) {
        openConnection();
        String sql = "INSERT INTO SessionInfo(shortName, fullName, dmID, nextSession) VALUES(?,?,?,?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shortName.toUpperCase());
            ps.setString(2, fullName);
            ps.setString(3, dmID);
            ps.setString(4, placeholder);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void openConnection() {
        createNewDatabase();
        createNewTables();
    }


    private static void createNewDatabase() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url);

                if (!new File(databaseFileLocation).exists()) {
                    if (connection != null) {
                        connection.getMetaData();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private static void createNewTables() {
        String infoSQL = "CREATE TABLE IF NOT EXISTS SessionInfo (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " shortName text NOT NULL,\n"
                + " fullName text,\n"
                + " dmID text NOT NULL,\n"
                + " nextSession text\n"
                + ");";

        String playersSQL = "CREATE TABLE IF NOT EXISTS SessionPlayers (\n"
                + " id INTEGER,\n"
                + " playerID text,\n"
                + "CONSTRAINT PK_SessionPlayers PRIMARY KEY(id, playerID)"
                + ");";

        if (connection == null) {
            createNewDatabase();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(infoSQL);
            stmt.execute(playersSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static String addSessionTime(String dmID, java.util.Date date) {
        openConnection();

        String shortName;
        try {
            shortName = getShortName(dmID);
            addSessionTimeToSpecificSession(shortName, date);
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }

        return shortName;
    }


    private static String getShortName(String dmID) throws SQLException {
        String sql = "SELECT shortName FROM SessionInfo WHERE dmID=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dmID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("shortName").toUpperCase();
            }
            else {
                throw new IllegalStateException("ID not in database");
            }
        }
    }


    public static void addSessionTimeToSpecificSession(String shortName, java.util.Date date) {
        String sql = "UPDATE SessionInfo SET nextSession=? WHERE shortName=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, setDateFormat.format(date));
            ps.setString(2, shortName.toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void addPlayer(String shortName, String playerID) {
        openConnection();
        String sql = "INSERT INTO SessionPlayers(id, playerID) VALUES(?,?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, getGameIDFromShortName(shortName.toUpperCase()));
            ps.setString(2, playerID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static int getGameIDFromShortName(String shortName) throws SQLException {
        openConnection();
        String sql = "SELECT id FROM SessionInfo WHERE shortName=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, shortName.toUpperCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
            else {
                throw new IllegalStateException("Short name not in database");
            }
        }
    }


    // TODO what if the DM has more than 1 campaign
    private static int getGameIDFromDMID(String dmID) throws SQLException {
        openConnection();
        String sql = "SELECT id FROM SessionInfo WHERE dmID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, dmID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
            else {
                throw new IllegalStateException("DM ID not in database");
            }
        }
    }


    private static String getShortName(int id) throws SQLException {
        openConnection();
        String sql = "SELECT shortName FROM SessionInfo WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("shortName").toUpperCase();
            }
            else {
                throw new IllegalStateException("ID not in database");
            }
        }
    }


    private static java.util.Date getNextSession(int id) throws SQLException {
        openConnection();
        String sql = "SELECT nextSession FROM SessionInfo WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String dateStr = rs.getString("nextSession");
                if (dateStr.equals(placeholder)) {
                    return null;
                }
                else {
                    return setDateFormat.parse(dateStr);
                }
            }
            else {
                throw new IllegalStateException("ID not in database");
            }
        } catch (ParseException e) {
            // Date cannot be invalid because it is set by formatting a util.Date
            throw new IllegalStateException("Dafak did you do?");
        }
    }


    public static Map<String, java.util.Date> getNextSessionTime(String playerID) {
        openConnection();
        Map<String, java.util.Date> map = new HashMap<>();
        String sql = "SELECT id FROM SessionPlayers WHERE playerID=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(getShortName(rs.getInt("id")), getNextSession(rs.getInt("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }


    public static String showDatabase() {
        openConnection();

        String sql1 = "SELECT * FROM SessionInfo";
        String sql2 = "SELECT * FROM SessionPlayers";
        StringBuilder outString = new StringBuilder();

        try (PreparedStatement ps1 = connection.prepareStatement(sql1);
             PreparedStatement ps2 = connection.prepareStatement(sql2))
        {
            ResultSet rs1 = ps1.executeQuery();
            ResultSet rs2 = ps2.executeQuery();

            while (rs1.next()) {
                outString.append(rs1.getInt("id"));
                outString.append(", ");
                outString.append(rs1.getString("shortName"));
                outString.append(", ");
                outString.append(rs1.getString("fullName"));
                outString.append(", ");
                outString.append(rs1.getString("dmID"));
                outString.append(", ");
                String dateStr = rs1.getString("nextSession");

                if (!dateStr.equals(placeholder)) {
                    outString.append(dateStr);
                }
                else {
                    outString.append("null");
                }

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
     * Returns string containing a mention to all players and a countdown to the next session
     */
    public static String getSessionReminder(String dmId, Guild guild) {
        try {
            final int gameID = getGameIDFromDMID(dmId);
            final List<String> playerIDs = getPlayerIDs(gameID);
            final List<Member> members = getMembers(guild, playerIDs);
            final Date nextSession = getNextSession(gameID);

            StringBuilder reminderString = new StringBuilder(
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


    private static List<String> getPlayerIDs(int id) throws SQLException {
        openConnection();
        String sql = "SELECT playerID FROM SessionPlayers WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            List<String> playerIDs = new ArrayList<>();
            if (rs.next()) {
                playerIDs.add(rs.getString("playerID"));

                while (rs.next()) {
                    playerIDs.add(rs.getString("playerID"));
                }

                return playerIDs;
            }
            else {
                throw new IllegalStateException("No players associated with this id");
            }
        }
    }


    private static List<Member> getMembers(Guild guild, List<String> ids) {
        List<Member> members = new ArrayList<>();
        for (String id : ids) {
            members.add(guild.getMemberById(id));
        }
        return members;
    }


    public static void removeGame(String shortName) {
        openConnection();

        String sqlInfo = "DELETE FROM SessionInfo WHERE id=?";
        String sqlPlayers = "DELETE FROM SessionPlayers WHERE id=?";

        try (PreparedStatement psInfo = connection.prepareStatement(sqlInfo);
             PreparedStatement psPlayers = connection.prepareStatement(sqlPlayers))
        {
            final int gameID = getGameIDFromShortName(shortName);

            psInfo.setInt(1, gameID);
            psPlayers.setInt(1, gameID);
            psInfo.executeUpdate();
            psPlayers.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static String getGamesList() {
        openConnection();

        StringBuilder stringBuilder = new StringBuilder("");

        String sql = "SELECT shortName, fullName FROM SessionInfo";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)
        )
        {
            while (rs.next()) {
                stringBuilder.append(rs.getString("shortName") + " - ");
                stringBuilder.append(rs.getString("fullName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}
