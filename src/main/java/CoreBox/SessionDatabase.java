package CoreBox;

import java.io.File;
import java.sql.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;



public class SessionDatabase {
    private static final String databaseFileLocation = Bot.getPathToJuuzoBot() + "Juuzo.db";
    private static final String url = "jdbc:sqlite:" + databaseFileLocation;
    private static Connection connection = null;

    private static final String placeholder = "000";


    private static void createNewDatabase() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url);

                if (!new File(databaseFileLocation).exists()) {
                    if (connection != null) {
                        connection.getMetaData();

                        System.out.println("newDBSuccess");
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
        System.out.println("newTablesSuccess");
    }


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
        System.out.println("clearTablesSuccess");
    }


    private static void openConnection() {
        createNewDatabase();
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
        System.out.println("addGameSuccess");
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
            ps.setString(1, SessionTimes.setDateFormat.format(date));
            ps.setString(2, shortName.toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("addSessionSuccess");
    }


    public static void addPlayer(String shortName, String playerID) {
        openConnection();
        String sql = "INSERT INTO SessionPlayers(id, playerID) VALUES(?,?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, getID(shortName.toUpperCase()));
            ps.setString(2, playerID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("AddPlayerSuccess");
    }


    private static int getID(String shortName) throws SQLException {
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
                    return SessionTimes.setDateFormat.parse(dateStr);
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

        System.out.println("getNextSessionSuccess");
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
}
