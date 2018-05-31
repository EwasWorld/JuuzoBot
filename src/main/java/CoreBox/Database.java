package CoreBox;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;



public class Database {
    private static final String databaseFileLocation = Bot.getPathToJuuzoBot() + "Juuzo.db";
    // Used to establish the connection to the database
    private static final String url = "jdbc:sqlite:" + databaseFileLocation;
    private static Connection connection = null;

    // Date format given as an argument and stored in the database
    public static String setDateFormatStr = "HH:mm dd/M/yy z";
    public static DateFormat setDateFormat = new SimpleDateFormat(setDateFormatStr);
    // Date format when printing the date
    public static DateFormat printDateFormat = new SimpleDateFormat("E dd MMM 'at' HH:mm z");


    /*
     * Fetches the existing database
     *      Creates a new database if one is not found
     */
    private static void fetchDatabase() {
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


    /*
     * Creates SessionInfo and SessionPlayers if they don't already exist
     */
    private static void createNewTables() {
        final String infoSQL = "CREATE TABLE IF NOT EXISTS SessionInfo (\n"
                + " shortName text PRIMARY KEY NOT NULL,\n"
                + " fullName text NOT NULL,\n"
                + " dmID text NOT NULL,\n"
                + " nextSession text\n"
                + " autoReminderSet bit NOT NULL\n"
                + ");";

        final String playersSQL = "CREATE TABLE IF NOT EXISTS SessionPlayers (\n"
                + " shortName text NOT NULL,\n"
                + " playerID text NOT NULL,\n"
                + "CONSTRAINT PK_SessionPlayers PRIMARY KEY(shortName, playerID)"
                + ");";

        final String quotesSQL = "CREATE TABLE IF NOT EXISTS Quotes (\n"
                + " quoteID int AUTO_INCREMENT,\n"
                + " author text NOT NULL,\n"
                + " date text NOT NULL,\n"
                + " message text NOT NULL,\n"
                + "PRIMARY KEY(quoteID)"
                + ");";

        if (connection == null) {
            fetchDatabase();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(infoSQL);
            stmt.execute(playersSQL);
            stmt.execute(quotesSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*
     * Make a connection to the database and create the required tables (if they aren't already there)
     */
    public static Connection openConnection() {
        fetchDatabase();
        createNewTables();

        return connection;
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


    /*
     * Deletes SessionInfo and SessionPlayers from the database
     */
    private static void deleteTables() {
        openConnection();

        final String infoSQL = "DROP TABLE IF EXISTS SessionInfo";
        final String playersSQL = "DROP TABLE IF EXISTS SessionPlayers";


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(infoSQL);
            stmt.execute(playersSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        createNewTables();
    }
}
